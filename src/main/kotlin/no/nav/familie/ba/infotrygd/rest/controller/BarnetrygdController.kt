package no.nav.familie.ba.infotrygd.rest.controller

import io.micrometer.core.annotation.Timed
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiImplicitParams
import io.swagger.annotations.ApiOperation
import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.familie.ba.infotrygd.rest.api.InfotrygdLøpendeBarnetrygdResponse
import no.nav.familie.ba.infotrygd.rest.api.InfotrygdÅpenSakResponse
import no.nav.familie.ba.infotrygd.service.BarnetrygdService
import no.nav.familie.ba.infotrygd.service.ClientValidator
import no.nav.familie.kontrakter.ba.infotrygd.InfotrygdSøkRequest
import no.nav.familie.kontrakter.ba.infotrygd.InfotrygdSøkResponse
import no.nav.security.token.support.core.api.Protected
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import no.nav.familie.ba.infotrygd.rest.api.InfotrygdSøkResponse as InfotrygdSøkResponseGammel
import no.nav.familie.kontrakter.ba.infotrygd.Sak as SakDto
import no.nav.familie.kontrakter.ba.infotrygd.Stønad as StønadDto


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

    @ApiOperation("Uttrekk personer med ytelse. F.eks OS OS for barnetrygd, UT EF for småbarnstillegg")
    @PostMapping(path = ["migrering"])
    fun migrering(@RequestBody request: MigreringRequest): ResponseEntity<Set<String>> {
        clientValidator.authorizeClient()

        return ResponseEntity.ok(
            barnetrygdService.finnPersonerKlarForMigrering(
                request.page,
                request.size,
                request.valg,
                request.undervalg,
                request.maksAntallBarn
            )
        )
    }

    data class MigreringRequest(
        val page: Int,
        val size: Int,
        val valg: String,
        val undervalg: String,
        val maksAntallBarn: Int = 99
    )

    private fun hentStønaderPåBrukereOgBarn(brukere: List<String>,
                                            barn: List<String>?,
                                            historikk: Boolean?): Pair<List<StønadDto>, List<StønadDto>> {
        val brukere = brukere.map { FoedselsNr(it) }
        val barn = barn?.map { FoedselsNr(it) } ?: emptyList()

        return Pair(barnetrygdService.findStønadByBrukerFnr(brukere, historikk),
                    barnetrygdService.findStønadByBarnFnr(barn, historikk))
    }
}

