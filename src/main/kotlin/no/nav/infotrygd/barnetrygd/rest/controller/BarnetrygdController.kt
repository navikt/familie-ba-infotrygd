package no.nav.infotrygd.barnetrygd.rest.controller

import io.micrometer.core.annotation.Timed
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiImplicitParams
import io.swagger.annotations.ApiOperation
import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.familie.kontrakter.ba.infotrygd.InfotrygdSøkRequest
import no.nav.familie.kontrakter.ba.infotrygd.InfotrygdSøkResponse
import no.nav.familie.kontrakter.ba.infotrygd.Sak
import no.nav.familie.kontrakter.ba.infotrygd.Stønad
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
    @Deprecated("/infotrygd/barnetrygd/stonad gjør samme jobben, men returnerer resultatet istedenfor å trekke konklusjon. Det kan gjøres client-side")
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

    @ApiOperation("Uttrekk fra tabellen \"BA_STOENAD_20\".")
        @PostMapping(path = ["stonad"], consumes = ["application/json"])
    @ApiImplicitParams(
        ApiImplicitParam(name = "request",
            dataType = "InfotrygdSøkRequest",
            value = "{\n  \"brukere\": [\"12345678910\"]," + "\n  \"barn\": [\n\"23456789101\",\n\"34567891012\"\n]\n}"))
    fun stønad(@RequestBody request: InfotrygdSøkRequest,
               @RequestParam(required = false) historikk: Boolean?): ResponseEntity<InfotrygdSøkResponse<Stønad>> {
        clientValidator.authorizeClient()

        val brukere = request.brukere.map { FoedselsNr(it) }
        val barn = request.barn?.takeUnless { it.isEmpty() }?.map { FoedselsNr(it) }

        return ResponseEntity.ok(
            InfotrygdSøkResponse(bruker = barnetrygdService.findStønadByBrukerFnr(brukere, historikk),
                barn = barnetrygdService.findStønadByBarnFnr(barn ?: emptyList(), historikk))
        )
    }

    @ApiOperation("Uttrekk fra tabellen \"SA_SAK_10\".")
    @PostMapping(path = ["saker"], consumes = ["application/json"])
    @ApiImplicitParams(
        ApiImplicitParam(name = "request",
            dataType = "InfotrygdSøkRequest",
            value = "{\n  \"brukere\": [\"12345678910\"]," + "\n  \"barn\": [\n\"23456789101\",\n\"34567891012\"\n]\n}"))
    fun saker(@RequestBody request: InfotrygdSøkRequest): ResponseEntity<InfotrygdSøkResponse<Sak>> {
        clientValidator.authorizeClient()

        val brukere = request.brukere.map { FoedselsNr(it) }
        val barn = request.barn?.takeUnless { it.isEmpty() }?.map { FoedselsNr(it) }

        return ResponseEntity.ok(InfotrygdSøkResponse(bruker = barnetrygdService.findSakerByBrukerFnr(brukere),
                                           barn = barnetrygdService.findSakerByBarnFnr(barn ?: emptyList())))
    }
}