package no.nav.familie.ba.infotrygd.rest.controller

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import io.micrometer.core.annotation.Timed
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.familie.ba.infotrygd.service.BarnetrygdService
import no.nav.familie.ba.infotrygd.service.TilgangskontrollService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate
import java.time.YearMonth
import io.swagger.v3.oas.annotations.parameters.RequestBody as ApiRequestBody

@ProtectedWithClaims(issuer = "azuread")
@RestController
@Timed(value = "infotrygd_historikk_pensjon_controller", percentiles = [0.5, 0.95])
@RequestMapping("/infotrygd/barnetrygd")
class PensjonController(
    private val barnetrygdService: BarnetrygdService,
    private val tilgangskontrollService: TilgangskontrollService
) {

    @Operation(summary = "Uttrekk barnetrygdperioder på en person fra en bestemet måned. Maks 3 år tilbake i tid")
    @PostMapping(path = ["pensjon"], consumes = ["application/json"])
    @ApiRequestBody(content = [Content(examples = [ExampleObject(value = """{"ident": "12345678910", "fraDato": "2022-12-01"}""")])])
    fun hentBarnetrygd(@RequestBody request: BarnetrygdTilPensjonRequest): BarnetrygdTilPensjonResponse {
        tilgangskontrollService.sjekkTilgang()

        val fraDato = YearMonth.of(request.fraDato.year, request.fraDato.month)

        if (fraDato.isBefore(YearMonth.now().minusYears(3))) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "fraDato kan ikke være lenger enn 3 år tilbake i tid")
        }

        val bruker = FoedselsNr(request.ident)

        return BarnetrygdTilPensjonResponse(
            saker = barnetrygdService.finnBarnetrygdForPensjon(bruker, fraDato)
        )
    }

    @Operation(summary = "Finner alle personer med barnetrygd innenfor et bestemt år på vegne av Psys")
    @GetMapping(path = ["pensjon"])
    fun personerMedBarnetrygd(@Parameter(name = "aar") @RequestParam("aar") år: String): List<FoedselsNr> {
        tilgangskontrollService.sjekkTilgang()
        return barnetrygdService.finnPersonerBarnetrygdPensjon(år)
    }


    data class BarnetrygdTilPensjonRequest(
        val ident: String,
        @Schema(implementation = String::class, example = "2020-12-01") val fraDato: LocalDate,
    )

    data class BarnetrygdTilPensjonResponse(
        @JsonProperty("fagsaker") val saker: List<BarnetrygdTilPensjon>
    )

    data class BarnetrygdTilPensjon(
        @JsonProperty("fagsakEiersIdent") val fnr: String,
        val barnetrygdPerioder: List<BarnetrygdPeriode>,
    )

    data class BarnetrygdPeriode(
        val personIdent: String,
        val delingsprosentYtelse: YtelseProsent,
        val ytelseTypeEkstern: YtelseTypeEkstern?,
        val utbetaltPerMnd: Int,
        val stønadFom: YearMonth,
        val stønadTom: YearMonth,
        val sakstypeEkstern: SakstypeEkstern,
        val kildesystem: String = "Infotrygd",
        val pensjonstrygdet: Boolean? = null,
        val norgeErSekundærland: Boolean? = null,
        @JsonIgnore
        val iverksatt: YearMonth? = null // kun til bruk som filtreringskriterie i tilfeller hvor to perioder overlapper fra dag 1
    )

    enum class YtelseTypeEkstern {
        ORDINÆR_BARNETRYGD,
        UTVIDET_BARNETRYGD,
        SMÅBARNSTILLEGG,
    }

    enum class SakstypeEkstern {
        NASJONAL,
        EØS
    }

    enum class YtelseProsent {
        FULL,
        DELT,
        USIKKER
    }
}
