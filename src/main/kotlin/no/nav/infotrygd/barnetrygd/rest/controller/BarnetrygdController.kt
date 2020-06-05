package no.nav.infotrygd.barnetrygd.rest.controller

import io.micrometer.core.annotation.Timed
import io.swagger.annotations.ApiOperation
import no.nav.infotrygd.barnetrygd.rest.api.InfotrygdSøkRequest
import no.nav.infotrygd.barnetrygd.rest.api.InfotrygdSøkResponse
import no.nav.infotrygd.barnetrygd.service.*
import no.nav.security.oidc.api.Protected
import org.springframework.web.bind.annotation.*

@Protected
@RestController
@Timed(value = "infotrygd_historikk_barnetrygd_controller", percentiles = [0.5, 0.95])
class BarnetrygdController(
    private val barnetrygdHistorikk: BarnetrygdHistorikkService,
    private val clientValidator: ClientValidator
) {

    @ApiOperation("søker etter oppgitte fødselssnummere og gir svar 'ingenTreff=true/false' ang. barnetrygd")
    @PostMapping(path = ["/infotrygd/barnetrygd/personsøk"])
    fun finnesIInfotrygd(@RequestBody
                         personListe: InfotrygdSøkRequest): InfotrygdSøkResponse {
        val finnes = barnetrygdHistorikk.finnes(personListe.brukere, personListe.barn)
        return InfotrygdSøkResponse(ingenTreff = !finnes)
    }
}