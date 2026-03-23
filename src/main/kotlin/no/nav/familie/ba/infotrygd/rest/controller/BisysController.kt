package no.nav.familie.ba.infotrygd.rest.controller

import io.micrometer.core.annotation.Timed
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.familie.ba.infotrygd.KonsumeresAv
import no.nav.familie.ba.infotrygd.service.BarnetrygdService
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.YearMonth
import io.swagger.v3.oas.annotations.parameters.RequestBody as ApiRequestBody

@PreAuthorize("hasRole('FORVALTER') or hasRole('APPLICATION') or hasRole('SAKSBEHANDLER')")
@RestController
@Timed(value = "infotrygd_historikk_bisys_controller", percentiles = [0.5, 0.95])
@RequestMapping("/infotrygd/barnetrygd")
class BisysController(
    private val barnetrygdService: BarnetrygdService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Operation(
        summary = "Uttrekk utvidet barnetrygd/småbarnstillegg utbetaling på en person fra en bestemet måned. Maks 5 år tilbake i tid",
    )
    @PostMapping(path = ["utvidet"], consumes = ["application/json"])
    @ApiRequestBody(content = [Content(examples = [ExampleObject(value = """{"personIdent": "12345678910", "fraDato": "2020-05"}""")])])
    @KonsumeresAv(apper = ["familie-ba-sak"])
    fun utvidet(
        @RequestBody request: InfotrygdUtvidetBarnetrygdRequest,
    ): InfotrygdUtvidetBarnetrygdResponse {

        val bruker = FoedselsNr(request.personIdent)

        return barnetrygdService.finnUtvidetBarnetrygdBisys(bruker, request.fraDato)
    }

    data class InfotrygdUtvidetBarnetrygdRequest(
        val personIdent: String,
        @Schema(implementation = String::class, example = "2020-05") val fraDato: YearMonth,
    )

    class InfotrygdUtvidetBarnetrygdResponse(
        val perioder: List<UtvidetBarnetrygdPeriode>,
    )

    data class UtvidetBarnetrygdPeriode(
        val stønadstype: Stønadstype,
        @Schema(implementation = String::class, example = "2020-05")
        val fomMåned: YearMonth,
        @Schema(implementation = String::class, example = "2020-12")
        val tomMåned: YearMonth?,
        val beløp: Double,
        val manueltBeregnet: Boolean,
        val deltBosted: Boolean,
    )

    enum class Stønadstype {
        UTVIDET,
        SMÅBARNSTILLEGG,
    }
}
