package no.nav.infotrygd.barnetrygd.rest.controller

import io.micrometer.core.annotation.Timed
import io.swagger.annotations.*
import no.nav.infotrygd.barnetrygd.rest.api.*
import no.nav.infotrygd.barnetrygd.service.BarnetrygdService
import no.nav.infotrygd.barnetrygd.service.ClientValidator
import no.nav.security.token.support.core.api.Protected
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@Protected
@RestController
@Timed(value = "infotrygd_historikk_barnetrygd_controller", percentiles = [0.5, 0.95])
class BarnetrygdController(
    private val barnetrygdService: BarnetrygdService,
    private val clientValidator: ClientValidator
) {

    @ApiOperation("Avgjør hvorvidt det finnes en løpende sak på søker eller barn i Infotrygd.")
    @PostMapping(path = ["/infotrygd/barnetrygd/lopendeSak"], consumes = ["application/json"])
    @ApiImplicitParams(
        ApiImplicitParam(name = "request",
                         dataType = "InfotrygdSøkRequest",
                         value = "{\n  \"brukere\": [\"12345678910\"]," + "\n  \"barn\": [\n\"23456789101\",\n\"34567891012\"\n]\n}"))
    @Deprecated("/infotrygd/barnetrygd/stonad gjør samme jobben, men returnerer resultatet istedenfor å trekke konklusjon. Det kan gjøres client-side")
    fun harLopendeBarnetrygdSak(@RequestBody request: InfotrygdSøkRequest): ResponseEntity<Any> {
        clientValidator.authorizeClient()

        if (request.brukere.isEmpty() && request.barn.isNullOrEmpty()) {
            return ResponseEntity.ok(InfotrygdSøkResponse(ingenTreff = true))
        }

        val mottarBarnetrygd = barnetrygdService.mottarBarnetrygd(request.brukere, request.barn?.takeUnless { it.isEmpty() })
        return ResponseEntity.ok(InfotrygdSøkResponse(ingenTreff = !mottarBarnetrygd))
    }

    @ApiOperation("Uttrekk fra tabellen \"BA_STOENAD_20\".")
    @PostMapping(path = ["/infotrygd/barnetrygd/stonad"], consumes = ["application/json"])
    @ApiImplicitParams(
        ApiImplicitParam(name = "request",
            dataType = "InfotrygdSøkRequest",
            value = "{\n  \"brukere\": [\"12345678910\"]," + "\n  \"barn\": [\n\"23456789101\",\n\"34567891012\"\n]\n}"))
    fun stønad(@RequestBody request: InfotrygdSøkRequest): ResponseEntity<StønadResult> {
        clientValidator.authorizeClient()
        return ResponseEntity.ok(StønadResult(bruker = barnetrygdService.findLøpendeStønadByBrukerFnr(request.brukere),
                                              barn = barnetrygdService.findLøpendeStønadByBarnFnr(request.barn ?: emptyList())))
    }

    @ApiOperation("Uttrekk fra tabellen \"SA_SAK_10\".")
    @PostMapping(path = ["/infotrygd/barnetrygd/saker"], consumes = ["application/json"])
    @ApiImplicitParams(
        ApiImplicitParam(name = "request",
            dataType = "InfotrygdSøkRequest",
            value = "{\n  \"brukere\": [\"12345678910\"]," + "\n  \"barn\": [\n\"23456789101\",\n\"34567891012\"\n]\n}"))
    fun saker(@RequestBody request: InfotrygdSøkRequest): ResponseEntity<SakResult> {
        clientValidator.authorizeClient()
        return ResponseEntity.ok(SakResult(bruker = barnetrygdService.findSakerByBrukerFnr(request.brukere),
                                           barn = barnetrygdService.findSakerByBarnFnr(request.barn ?: emptyList())))
    }
}