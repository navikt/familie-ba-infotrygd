package no.nav.infotrygd.barnetrygd.rest.controller

import io.micrometer.core.annotation.Timed
import io.swagger.annotations.*
import no.nav.infotrygd.barnetrygd.rest.api.InfotrygdSøkRequest
import no.nav.infotrygd.barnetrygd.rest.api.InfotrygdSøkResponse
import no.nav.infotrygd.barnetrygd.service.BarnetrygdService
import no.nav.infotrygd.barnetrygd.service.ClientValidator
import no.nav.security.oidc.api.Protected
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@Protected
@RestController
@Timed(value = "infotrygd_historikk_barnetrygd_controller", percentiles = [0.5, 0.95])
class BarnetrygdController(
    private val barnetrygdService: BarnetrygdService,
    private val clientValidator: ClientValidator
) {

    @ApiOperation("søker etter oppgitte fødselssnummere og gir svar 'ingenTreff=true/false' ang. barnetrygd")
    @PostMapping(path = ["/infotrygd/barnetrygd/personsok"], consumes = ["application/json"])
    @ApiImplicitParams(
        ApiImplicitParam(name = "request",
            dataType = "InfotrygdSøkRequest",
            value = "{\n  \"brukere\": [\n\"01015450301\"\n]," + "\n  \"barn\": [\n\"01015450300\",\n\"01015450572\"\n]\n}"))
    fun finnesIInfotrygd(@RequestBody request: InfotrygdSøkRequest): ResponseEntity<Any> {
        clientValidator.authorizeClient()

        request.takeUnless { it.brukere.isEmpty() && it.barn.isNullOrEmpty() } ?:
        return ResponseEntity("Tom personListe", HttpStatus.BAD_REQUEST)

        val finnes = barnetrygdService.finnes(request.brukere, request.barn)
        return ResponseEntity.ok(InfotrygdSøkResponse(ingenTreff = !finnes))
    }

    @ApiOperation("Avgjør hvorvidt det finens en løpende sak på søker eller barn i Infotrygd.")
    @PostMapping(path = ["/infotrygd/barnetrygd/lopendeSak"], consumes = ["application/json"])
    @ApiImplicitParams(
        ApiImplicitParam(name = "request",
                         dataType = "InfotrygdSøkRequest",
                         value = "{\n  \"brukere\": [\n\"01015450301\"\n]," + "\n  \"barn\": [\n\"01015450300\",\n\"01015450572\"\n]\n}"))
    fun harLopendeBarnetrygdSak(@RequestBody request: InfotrygdSøkRequest): ResponseEntity<Any> {
        clientValidator.authorizeClient()

        request.takeUnless { it.brukere.isEmpty() && it.barn.isNullOrEmpty() } ?:
                return ResponseEntity("Tom personListe", HttpStatus.BAD_REQUEST)

        val mottarBarnetrygd = barnetrygdService.mottarBarnetrygd(request.brukere, request.barn)
        return ResponseEntity.ok(InfotrygdSøkResponse(ingenTreff = !mottarBarnetrygd))
    }
}