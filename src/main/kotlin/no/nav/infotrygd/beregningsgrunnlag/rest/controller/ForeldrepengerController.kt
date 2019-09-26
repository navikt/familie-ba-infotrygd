package no.nav.infotrygd.beregningsgrunnlag.rest.controller

import no.nav.infotrygd.beregningsgrunnlag.dto.Foreldrepenger
import no.nav.infotrygd.beregningsgrunnlag.service.ClientValidator
import no.nav.infotrygd.beregningsgrunnlag.service.ForeldrepengerService
import no.nav.infotrygd.beregningsgrunnlag.values.FodselNr
import no.nav.security.oidc.api.Protected
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@Protected
class ForeldrepengerController(
    private val foreldrepengerService: ForeldrepengerService,
    private val clientValidator: ClientValidator
) {
    @GetMapping(path = ["/foreldrepenger"])
    fun foreldrepenger(@RequestParam
                       fodselNr: String,

                       @RequestParam
                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                       fom: LocalDate,

                       @RequestParam(required = false)
                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                       tom: LocalDate?) : List<Foreldrepenger> {

        clientValidator.authorizeClient()
        return foreldrepengerService.hentForeldrepenger(FodselNr(fodselNr), fom, tom)
    }
}