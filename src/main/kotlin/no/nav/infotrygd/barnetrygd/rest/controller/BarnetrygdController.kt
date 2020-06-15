package no.nav.infotrygd.barnetrygd.rest.controller

import io.micrometer.core.annotation.Timed
import io.swagger.annotations.ApiOperation
import no.nav.infotrygd.barnetrygd.rest.api.InfotrygdSøkRequest
import no.nav.infotrygd.barnetrygd.rest.api.InfotrygdSøkResponse
import no.nav.infotrygd.barnetrygd.service.BarnetrygdHistorikkService
import no.nav.infotrygd.barnetrygd.service.ClientValidator
import no.nav.security.oidc.api.Protected
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@Protected
@RestController
@Timed(value = "infotrygd_historikk_barnetrygd_controller", percentiles = [0.5, 0.95])
class BarnetrygdController(
    private val barnetrygdHistorikk: BarnetrygdHistorikkService,
    private val clientValidator: ClientValidator
) {

    @ApiOperation("søker etter oppgitte fødselssnummere og gir svar 'ingenTreff=true/false' ang. barnetrygd")
    @PostMapping(path = ["/infotrygd/barnetrygd/personsøk"], consumes = ["application/json"])
    fun finnesIInfotrygd(@RequestBody request: InfotrygdSøkRequest): ResponseEntity<Any> {
        clientValidator.authorizeClient()

        request.takeUnless { it.brukere.isEmpty() && it.barn.isNullOrEmpty() } ?:
                return ResponseEntity("Tom personListe", HttpStatus.BAD_REQUEST)

        val finnes = barnetrygdHistorikk.finnes(request.brukere, request.barn)
        return ResponseEntity.ok(InfotrygdSøkResponse(ingenTreff = !finnes))
    }
}