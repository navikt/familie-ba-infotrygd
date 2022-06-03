package no.nav.familie.ba.infotrygd.rest.controller

import io.micrometer.core.annotation.Timed
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import no.nav.familie.ba.infotrygd.service.BarnetrygdService
import no.nav.familie.ba.infotrygd.service.ClientValidator
import no.nav.familie.eksterne.kontrakter.skatteetaten.SkatteetatenPeriode
import no.nav.familie.eksterne.kontrakter.skatteetaten.SkatteetatenPerioderRequest
import no.nav.familie.eksterne.kontrakter.skatteetaten.SkatteetatenPerioderResponse
import no.nav.familie.eksterne.kontrakter.skatteetaten.SkatteetatenPersonerResponse
import no.nav.familie.log.mdc.MDCConstants
import no.nav.security.token.support.core.api.Protected
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID
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
    private val secureLogger = LoggerFactory.getLogger("secureLogger")


    @Operation(summary = "Hent alle perioder for utvidet for en liste personer")
    @PostMapping(path = ["utvidet/skatteetaten/perioder"], consumes = ["application/json"])
    @ApiRequestBody(content = [Content(examples = [ExampleObject(value = """{"identer": ["12345678910"], "aar": "2020"}""")])])
    fun skatteetatenPerioderUtvidetPersoner(
        @RequestBody
        request: SkatteetatenPerioderRequest
    ): List<SkatteetatenPerioderResponse> {
        clientValidator.authorizeClient()

        return request.identer.map {
            barnetrygdService.finnPerioderUtvidetBarnetrygdSkatt(it, request.aar.toInt())
        }
    }

    @Operation(summary = "Finner alle personer med utvidet barnetrygd innenfor et bestemt år")
    @GetMapping(path = ["utvidet"])
    fun personerMedUtvidet(@Parameter(name = "aar") @RequestParam("aar") år: String): SkatteetatenPersonerResponse {
        clientValidator.authorizeClient()
        return SkatteetatenPersonerResponse(brukere = barnetrygdService.finnPersonerUtvidetBarnetrygdSkatt(år))
    }

    @Operation(summary = "Finner alle personer med utvidet barnetrygd innenfor et bestemt år")
    @GetMapping(path = ["delingsprosent"])
    fun identifiserAntallUsikkerDelingsprosent(@Parameter(name = "aar") @RequestParam("aar") år: String): String {
        clientValidator.authorizeClient()


        val allePersoner = personerMedUtvidet(år).brukere
        logger.info("Hentet personer med utvidet ${allePersoner.size} for  $år")


        GlobalScope.launch {
            try {
                MDC.put(MDCConstants.MDC_CALL_ID, UUID.randomUUID().toString() + år)
                val identerMedUsikkerDelingsprosent = mutableSetOf<String>()
                allePersoner.chunked(10000) {
                    logger.info("Sjekket ${it.size} nye identer etter delingsprosent usikker for  $år")
                    it.forEach { skatteetatenPerson ->
                        val perioder = barnetrygdService.finnPerioderUtvidetBarnetrygdSkatt(skatteetatenPerson.ident, år.toInt())
                        val periode = perioder.brukere.firstOrNull()
                        val harUsikkerDelingsprosent =
                            periode?.perioder?.any { it.delingsprosent == SkatteetatenPeriode.Delingsprosent.usikker }
                        if (harUsikkerDelingsprosent == true) {
                            secureLogger.info("${skatteetatenPerson.ident} har usikker delingsprosent")
                            identerMedUsikkerDelingsprosent.add(skatteetatenPerson.ident)
                        }
                    }
                }

                val identerGruppertByUndervalg = mutableMapOf<String, MutableSet<String>>()

                identerMedUsikkerDelingsprosent.forEach {
                    val alleUndervalg = barnetrygdService.listUtvidetStønadstyperForPerson(år.toInt(), fnr = it )

                        alleUndervalg.forEach { undervalg ->
                            if (identerGruppertByUndervalg.containsKey(undervalg)) {
                                identerGruppertByUndervalg[undervalg]!!.add(it)
                            } else {
                                identerGruppertByUndervalg[undervalg] = mutableSetOf(it)
                            }
                        }
                }

                identerGruppertByUndervalg.keys.forEach { undervalg ->
                    logger.info("Usikker delingsprosent $år: $undervalg: ${identerGruppertByUndervalg[undervalg]?.size}")
                    identerGruppertByUndervalg[undervalg]?.chunked(1000){ chunk ->
                        secureLogger.info("Usikker delingsprosent $år: $undervalg: ${chunk.size} $chunk")
                    }
                }
                logger.info("Ferdig med å sjekke etter delingsprosent usikker for $år. Fant ${identerMedUsikkerDelingsprosent.size} totalt")
            } finally {
                MDC.clear()
            }
        }
        return "Sjekker ${allePersoner.size} saker for usikker delingsprosent. Sjekk securelogs"
    }
}





