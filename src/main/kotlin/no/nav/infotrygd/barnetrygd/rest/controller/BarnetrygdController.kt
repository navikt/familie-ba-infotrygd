package no.nav.infotrygd.barnetrygd.rest.controller

import io.micrometer.core.annotation.Timed
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiImplicitParams
import io.swagger.annotations.ApiModelProperty
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.familie.eksterne.kontrakter.skatteetaten.SkatteetatenPerioderRequest
import no.nav.familie.eksterne.kontrakter.skatteetaten.SkatteetatenPerioderResponse
import no.nav.familie.eksterne.kontrakter.skatteetaten.SkatteetatenPerson
import no.nav.familie.eksterne.kontrakter.skatteetaten.SkatteetatenPersonerResponse
import no.nav.familie.kontrakter.ba.infotrygd.InfotrygdSøkRequest
import no.nav.familie.kontrakter.ba.infotrygd.InfotrygdSøkResponse
import no.nav.infotrygd.barnetrygd.rest.api.InfotrygdLøpendeBarnetrygdResponse
import no.nav.infotrygd.barnetrygd.rest.api.InfotrygdÅpenSakResponse
import no.nav.infotrygd.barnetrygd.service.BarnetrygdService
import no.nav.infotrygd.barnetrygd.service.ClientValidator
import no.nav.security.token.support.core.api.Protected
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime
import java.time.YearMonth
import no.nav.familie.kontrakter.ba.infotrygd.Sak as SakDto
import no.nav.familie.kontrakter.ba.infotrygd.Stønad as StønadDto
import no.nav.infotrygd.barnetrygd.rest.api.InfotrygdSøkResponse as InfotrygdSøkResponseGammel


@Protected
@RestController
@Timed(value = "infotrygd_historikk_barnetrygd_controller", percentiles = [0.5, 0.95])
@RequestMapping("/infotrygd/barnetrygd")
class BarnetrygdController(
    private val barnetrygdService: BarnetrygdService,
    private val clientValidator: ClientValidator
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @ApiOperation("Avgjør hvorvidt det finnes en løpende sak på søker eller barn i Infotrygd.")
    @PostMapping(path = ["lopendeSak"], consumes = ["application/json"])
    @ApiImplicitParams(
        ApiImplicitParam(name = "request",
                         dataType = "InfotrygdSøkRequest",
                         value = "{\n  \"brukere\": [\"12345678910\"]," + "\n  \"barn\": [\n\"23456789101\",\n\"34567891012\"\n]\n}"))
    @Deprecated("Bruk /lopende-barnetrygd")
    fun harLopendeBarnetrygdSak(@RequestBody request: InfotrygdSøkRequest): ResponseEntity<Any> {
        clientValidator.authorizeClient()

        if (request.brukere.isEmpty() && request.barn.isNullOrEmpty()) {
            return ResponseEntity.ok(InfotrygdSøkResponseGammel(ingenTreff = true))
        }

        val brukere = request.brukere.map { FoedselsNr(it) }
        val barn = request.barn?.takeUnless { it.isEmpty() }?.map { FoedselsNr(it) }

        val mottarBarnetrygd = barnetrygdService.mottarBarnetrygd(brukere, barn)
        return ResponseEntity.ok(InfotrygdSøkResponseGammel(ingenTreff = !mottarBarnetrygd))
    }

    @ApiOperation("Avgjør hvorvidt det finnes løpende barnetrygd på søker eller barn i Infotrygd.")
    @PostMapping(path = ["lopende-barnetrygd"], consumes = ["application/json"])
    @ApiImplicitParams(
        ApiImplicitParam(name = "request",
            dataType = "InfotrygdSøkRequest",
            value = "{\n  \"brukere\": [\"12345678910\"]," + "\n  \"barn\": [\n\"23456789101\",\n\"34567891012\"\n]\n}"))
    fun harLopendeBarnetrygd(@RequestBody request: InfotrygdSøkRequest): ResponseEntity<InfotrygdLøpendeBarnetrygdResponse> {
        clientValidator.authorizeClient()

        val harLøpendeBarnetrygd = hentStønaderPåBrukereOgBarn(request.brukere, request.barn, false).let {
            it.first.isNotEmpty() || it.second.isNotEmpty()
        }

        return ResponseEntity.ok(InfotrygdLøpendeBarnetrygdResponse(harLøpendeBarnetrygd))
    }

    @ApiOperation("Svarer hvorvidt det finnes en åpen sak til beslutning, på søker eller barn i Infotrygd.")
    @PostMapping(path = ["aapen-sak"], consumes = ["application/json"])
    @ApiImplicitParams(
        ApiImplicitParam(name = "request",
            dataType = "InfotrygdSøkRequest",
            value = "{\n  \"brukere\": [\"12345678910\"]," + "\n  \"barn\": [\n\"23456789101\",\n\"34567891012\"\n]\n}"))
    fun harÅpenSak(@RequestBody request: InfotrygdSøkRequest): ResponseEntity<InfotrygdÅpenSakResponse> {
        clientValidator.authorizeClient()

        return barnetrygdService.tellAntallÅpneSaker(request.brukere, request.barn).let {
            ResponseEntity.ok(InfotrygdÅpenSakResponse(it > 0))
        }
    }

    @ApiOperation("Uttrekk fra tabellen \"BA_STOENAD_20\".")
        @PostMapping(path = ["stonad"], consumes = ["application/json"])
    @ApiImplicitParams(
        ApiImplicitParam(name = "request",
            dataType = "InfotrygdSøkRequest",
            value = "{\n  \"brukere\": [\"12345678910\"]," + "\n  \"barn\": [\n\"23456789101\",\n\"34567891012\"\n]\n}"))
    fun stønad(@RequestBody request: InfotrygdSøkRequest,
               @RequestParam(required = false) historikk: Boolean?): ResponseEntity<InfotrygdSøkResponse<StønadDto>> {
        clientValidator.authorizeClient()

        return hentStønaderPåBrukereOgBarn(request.brukere, request.barn, historikk).let {
            ResponseEntity.ok(InfotrygdSøkResponse(bruker = it.first, barn = it.second))
        }
    }

    @ApiOperation("Uttrekk fra tabellen \"SA_SAK_10\".")
    @PostMapping(path = ["saker"], consumes = ["application/json"])
    @ApiImplicitParams(
        ApiImplicitParam(name = "request",
            dataType = "InfotrygdSøkRequest",
            value = "{\n  \"brukere\": [\"12345678910\"]," + "\n  \"barn\": [\n\"23456789101\",\n\"34567891012\"\n]\n}"))
    fun saker(@RequestBody request: InfotrygdSøkRequest): ResponseEntity<InfotrygdSøkResponse<SakDto>> {
        clientValidator.authorizeClient()

        val brukere = request.brukere.map { FoedselsNr(it) }
        val barn = request.barn?.takeUnless { it.isEmpty() }?.map { FoedselsNr(it) }

        return ResponseEntity.ok(InfotrygdSøkResponse(bruker = barnetrygdService.findSakerByBrukerFnr(brukere),
                                           barn = barnetrygdService.findSakerByBarnFnr(barn ?: emptyList())))
    }


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

    @ApiOperation("Uttrekk utvidet barnetrygd/småbarnstillegg utbetaling på en person fra en bestemet måned. Maks 5 år tilbake i tid")
    @PostMapping(path = ["utvidet/skatteetaten"], consumes = ["application/json"])
    @ApiImplicitParams(
        ApiImplicitParam(name = "request",
            dataType = "InfotrygdUtvidetBarnetrygdRequest",
            value = """{"bruker": "12345678910", "år": "2020"}"""))
    @Deprecated("bruk skatteetatenPerioderUtvidetPersoner")
    fun skatteetatenPerioderUtvidetPerson(@RequestBody request: SkatteetatenPerioderUtvidetRequest): SkatteetatenPerioderResponse {
        clientValidator.authorizeClient()

        return barnetrygdService.finnPerioderMedUtvidetBarnetrygdForÅr(request.personIdent, request.år)
    }


    @ApiOperation("Hent alle perioder for utvidet for en liste personer")
    @PostMapping(path = ["utvidet/skatteetaten/perioder"], consumes = ["application/json"])
    @ApiImplicitParams(
        ApiImplicitParam(name = "request",
                         dataType = "SkatteetatenPerioderRequest",
                         value = """{"identer": ["12345678910"], "aar": 2020}"""))
    fun skatteetatenPerioderUtvidetPersoner(@RequestBody request: SkatteetatenPerioderRequest): List<SkatteetatenPerioderResponse> {
        clientValidator.authorizeClient()

        return request.identer.map{
            barnetrygdService.finnPerioderMedUtvidetBarnetrygdForÅr(it, request.aar.toInt())
        }
    }


    @ApiOperation("Finner alle personer med utvidet barnetrygd innenfor et bestemt år")
    @GetMapping(path =["utvidet"])
    fun utvidet(@ApiParam("år") @RequestParam("aar") år: String): SkatteetatenPersonerResponse {
        clientValidator.authorizeClient()
        return SkatteetatenPersonerResponse(brukere = barnetrygdService.finnPersonerMedUtvidetBarnetrygd(år))
    }

    data class InfotrygdUtvidetBarnetrygdRequest( val personIdent: String,
                                                  @ApiModelProperty(dataType = "java.lang.String", example = "2020-05") val fraDato: YearMonth)


    data class SkatteetatenPerioderUtvidetRequest( val personIdent: String,
                                                   val år: Int)

    class InfotrygdUtvidetBarnetrygdResponse(val perioder: List<UtvidetBarnetrygdPeriode>)



    data class UtvidetBarnetrygdPeriode(val stønadstype: Stønadstype,
                                        @ApiModelProperty(dataType = "java.lang.String", example = "2020-05")
                                        val fomMåned: YearMonth,
                                        @ApiModelProperty(dataType = "java.lang.String", example = "2020-12")
                                        val tomMåned: YearMonth?,
                                        val beløp: Double,
                                        val manueltBeregnet: Boolean,
    )

    class InfotrygdUtvidetBaPersonerResponse(val brukere: List<UtvidetBarnetrygdPerson>)
    data class UtvidetBarnetrygdPerson(val ident: String,
                                       val sisteVedtakPaaIdent: LocalDateTime)

    enum class Stønadstype {
        UTVIDET,
        SMÅBARNSTILLEGG
    }


    @ApiOperation("Uttrekk personer med ytelse. F.eks OS OS for barnetrygd, UT EF for småbarnstillegg")
    @GetMapping(path = ["liste-lopende-sak"])
    fun hentLøpendeBarnetrygdFnr(@RequestParam("valg") valg: String, @RequestParam("undervalg") undervalg: String, @RequestParam("page") page: Int = 0): ResponseEntity<Set<String>> {
        clientValidator.authorizeClient()

        return ResponseEntity.ok(barnetrygdService.hentLøpendeStønader(valg, undervalg, page))
    }



    private fun hentStønaderPåBrukereOgBarn(brukere: List<String>,
                                            barn: List<String>?,
                                            historikk: Boolean?): Pair<List<StønadDto>, List<StønadDto>> {
        val brukere = brukere.map { FoedselsNr(it) }
        val barn = barn?.map { FoedselsNr(it) } ?: emptyList()

        return Pair(barnetrygdService.findStønadByBrukerFnr(brukere, historikk),
                    barnetrygdService.findStønadByBarnFnr(barn, historikk))
    }
}

