package no.nav.infotrygd.barnetrygd.rest.controller

import io.micrometer.core.annotation.Timed
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiImplicitParams
import io.swagger.annotations.ApiOperation
import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.familie.kontrakter.ba.infotrygd.InfotrygdSøkRequest
import no.nav.familie.kontrakter.ba.infotrygd.InfotrygdSøkResponse
import no.nav.infotrygd.barnetrygd.rest.api.InfotrygdLøpendeBarnetrygdResponse
import no.nav.familie.kontrakter.ba.infotrygd.Stønad as StønadDto
import no.nav.familie.kontrakter.ba.infotrygd.Sak as SakDto
import no.nav.infotrygd.barnetrygd.service.BarnetrygdService
import no.nav.infotrygd.barnetrygd.service.ClientValidator
import no.nav.security.token.support.core.api.Protected
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import no.nav.infotrygd.barnetrygd.rest.api.InfotrygdSøkResponse as InfotrygdSøkResponseGammel


@Protected
@RestController
@Timed(value = "infotrygd_historikk_barnetrygd_controller", percentiles = [0.5, 0.95])
@RequestMapping("/infotrygd/barnetrygd")
class BarnetrygdController(
    private val barnetrygdService: BarnetrygdService,
    private val clientValidator: ClientValidator
) {

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

    private fun hentStønaderPåBrukereOgBarn(brukere: List<String>,
                                            barn: List<String>?,
                                            historikk: Boolean?): Pair<List<StønadDto>, List<StønadDto>> {
        val brukere = brukere.map { FoedselsNr(it) }
        val barn = barn?.map { FoedselsNr(it) } ?: emptyList()

        return Pair(barnetrygdService.findStønadByBrukerFnr(brukere, historikk),
                    barnetrygdService.findStønadByBarnFnr(barn, historikk))
    }
}