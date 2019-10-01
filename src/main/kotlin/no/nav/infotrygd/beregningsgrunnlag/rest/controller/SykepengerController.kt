package no.nav.infotrygd.beregningsgrunnlag.rest.controller

import no.nav.infotrygd.beregningsgrunnlag.dto.Sykepenger
import no.nav.infotrygd.beregningsgrunnlag.service.ClientValidator
import no.nav.infotrygd.beregningsgrunnlag.service.SykepengerService
import no.nav.infotrygd.beregningsgrunnlag.values.FoedselNr
import no.nav.security.oidc.api.Protected
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@Protected
@RestController
class SykepengerController(
    private val sykepengerService: SykepengerService,
    private val clientValidator: ClientValidator
) {

    @GetMapping(path = ["/sykepenger"])
    fun sykepenger(@RequestParam
                   fodselNr: String,

                   @RequestParam
                   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                   fom: LocalDate,

                   @RequestParam(required = false)
                   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                   tom: LocalDate?): List<Sykepenger> {
        clientValidator.authorizeClient()
        return sykepengerService.hentSykepenger(FoedselNr(fodselNr), fom, tom)
    }
}