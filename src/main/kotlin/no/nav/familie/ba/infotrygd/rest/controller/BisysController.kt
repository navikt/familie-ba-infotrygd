package no.nav.familie.ba.infotrygd.rest.controller

import io.micrometer.core.annotation.Timed
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiImplicitParams
import io.swagger.annotations.ApiModelProperty
import io.swagger.annotations.ApiOperation
import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.familie.ba.infotrygd.service.BarnetrygdService
import no.nav.familie.ba.infotrygd.service.ClientValidator
import no.nav.security.token.support.core.api.Protected
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime
import java.time.YearMonth


@Protected
@RestController
@Timed(value = "infotrygd_historikk_bisys_controller", percentiles = [0.5, 0.95])
@RequestMapping("/infotrygd/barnetrygd")
class BisysController(
    private val barnetrygdService: BarnetrygdService,
    private val clientValidator: ClientValidator
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @ApiOperation("Uttrekk utvidet barnetrygd/småbarnstillegg utbetaling på en person fra en bestemet måned. Maks 5 år tilbake i tid")
    @PostMapping(path = ["utvidet"], consumes = ["application/json"])
    @ApiImplicitParams(
        ApiImplicitParam(name = "request",
                         dataType = "InfotrygdUtvidetBarnetrygdRequest",
                         value = """{"bruker": "12345678910", "fraDato": "2020-05"}"""))
    fun utvidet(@RequestBody request: InfotrygdUtvidetBarnetrygdRequest): InfotrygdUtvidetBarnetrygdResponse {
        clientValidator.authorizeClient()

        if (request.fraDato.isBefore(YearMonth.now().minusYears(5)))
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "fraDato kan ikke være lenger enn 5 år tilbake i tid")

        val bruker = FoedselsNr(request.personIdent)

        return barnetrygdService.finnUtvidetBarnetrygd(bruker, request.fraDato)
    }


    data class InfotrygdUtvidetBarnetrygdRequest( val personIdent: String,
                                                  @ApiModelProperty(dataType = "java.lang.String", example = "2020-05") val fraDato: YearMonth)




    class InfotrygdUtvidetBarnetrygdResponse(val perioder: List<UtvidetBarnetrygdPeriode>)



    data class UtvidetBarnetrygdPeriode(val stønadstype: Stønadstype,
                                        @ApiModelProperty(dataType = "java.lang.String", example = "2020-05")
                                        val fomMåned: YearMonth,
                                        @ApiModelProperty(dataType = "java.lang.String", example = "2020-12")
                                        val tomMåned: YearMonth?,
                                        val beløp: Double,
                                        val manueltBeregnet: Boolean,
                                        val deltBosted: Boolean
    )

    class InfotrygdUtvidetBaPersonerResponse(val brukere: List<UtvidetBarnetrygdPerson>)
    data class UtvidetBarnetrygdPerson(val ident: String,
                                       val sisteVedtakPaaIdent: LocalDateTime)

    enum class Stønadstype {
        UTVIDET,
        SMÅBARNSTILLEGG
    }
}
