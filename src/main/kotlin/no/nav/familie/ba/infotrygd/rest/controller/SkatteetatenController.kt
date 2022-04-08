package no.nav.familie.ba.infotrygd.rest.controller

import io.micrometer.core.annotation.Timed
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import no.nav.familie.ba.infotrygd.service.BarnetrygdService
import no.nav.familie.ba.infotrygd.service.ClientValidator
import no.nav.familie.eksterne.kontrakter.skatteetaten.SkatteetatenPerioderRequest
import no.nav.familie.eksterne.kontrakter.skatteetaten.SkatteetatenPerioderResponse
import no.nav.familie.eksterne.kontrakter.skatteetaten.SkatteetatenPersonerResponse
import no.nav.security.token.support.core.api.Protected
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import io.swagger.v3.oas.annotations.parameters.RequestBody as ApiRequestBody


@Protected
@RestController
@Timed(value = "infotrygd_historikk_skatt_controller", percentiles = [0.5, 0.95])
@RequestMapping("/infotrygd/barnetrygd")
class SkatteetatenController(
    private val barnetrygdService: BarnetrygdService,
    private val clientValidator: ClientValidator
) {

    private val logger = LoggerFactory.getLogger(javaClass)


    @Operation(summary = "Hent alle perioder for utvidet for en liste personer")
    @PostMapping(path = ["utvidet/skatteetaten/perioder"], consumes = ["application/json"])
    @ApiRequestBody(content = [Content(examples = [ExampleObject(value = """{"identer": ["12345678910"], "aar": 2020}""")])])
    fun skatteetatenPerioderUtvidetPersoner(
        @RequestBody
        request: SkatteetatenPerioderRequest
    ): List<SkatteetatenPerioderResponse> {
        clientValidator.authorizeClient()

        return request.identer.map {
            barnetrygdService.finnPerioderMedUtvidetBarnetrygdForÅr(it, request.aar.toInt())
        }
    }

    @Operation(summary = "Finner alle personer med utvidet barnetrygd innenfor et bestemt år")
    @GetMapping(path = ["utvidet"])
    fun personerMedUtvidet(@Parameter(name = "aar") @RequestParam("aar") år: String): SkatteetatenPersonerResponse {
        clientValidator.authorizeClient()
        return SkatteetatenPersonerResponse(brukere = barnetrygdService.finnPersonerMedUtvidetBarnetrygd(år))
    }
}

