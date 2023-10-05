package no.nav.familie.ba.infotrygd.rest.controller

import com.fasterxml.jackson.annotation.JsonProperty
import io.micrometer.core.annotation.Timed
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.familie.ba.infotrygd.service.BarnetrygdService
import no.nav.familie.ba.infotrygd.service.ClientValidator
import no.nav.security.token.support.core.api.Protected
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.time.YearMonth
import io.swagger.v3.oas.annotations.parameters.RequestBody as ApiRequestBody

@Protected
@RestController
@Timed(value = "infotrygd_historikk_pensjon_controller", percentiles = [0.5, 0.95])
@RequestMapping("/infotrygd/barnetrygd")
class PensjonController(
    private val barnetrygdService: BarnetrygdService,
    private val clientValidator: ClientValidator,
) {

    @Operation(summary = "Uttrekk barnetrygdperioder på en person fra en bestemet måned. Maks 2 år tilbake i tid")
    @PostMapping(path = ["pensjon"], consumes = ["application/json"])
    @ApiRequestBody(content = [Content(examples = [ExampleObject(value = """{"personIdent": "12345678910", "fraDato": "2022-05"}""")])])
    fun hentBarnetrygd(@RequestBody request: BarnetrygdTilPensjonRequest): BarnetrygdTilPensjonResponse {
        clientValidator.authorizeClient()

        if (request.fraDato.isBefore(YearMonth.now().minusYears(2))) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "fraDato kan ikke være lenger enn 2 år tilbake i tid")
        }

        val bruker = FoedselsNr(request.personIdent)

        return BarnetrygdTilPensjonResponse(
            saker = barnetrygdService.finnBarnetrygdForPensjon(bruker, request.fraDato)
        )
    }

    data class BarnetrygdTilPensjonRequest(
        val personIdent: String,
        @Schema(implementation = String::class, example = "2020-05") val fraDato: YearMonth,
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
        val kildesystem: String = "Infotrygd"
    )

    enum class YtelseTypeEkstern {
        ORDINÆR_BARNETRYGD,
        UTVIDET_BARNETRYGD,
        SMÅBARNSTILLEGG,
    }

    enum class YtelseProsent {
        FULL,
        DELT,
        USIKKER
    }
}
