package no.nav.infotrygd.beregningsgrunnlag.rest.controller

import no.nav.infotrygd.beregningsgrunnlag.dto.PaaroerendeSykdom
import no.nav.infotrygd.beregningsgrunnlag.service.ClientValidator
import no.nav.infotrygd.beregningsgrunnlag.service.PaaroerendeSykdomService
import no.nav.infotrygd.beregningsgrunnlag.values.FoedselNr
import no.nav.security.oidc.api.Protected
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@Protected
@RestController
class PaaroerendeSykdomController(
    private val paaroerendeSykdomService: PaaroerendeSykdomService,
    private val clientValidator: ClientValidator
) {
    @GetMapping(path = ["/paaroerendeSykdom"])
    fun paaroerendeSykdom(@RequestParam
                   fodselNr: String,

                   @RequestParam
                   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                   fom: LocalDate,

                   @RequestParam(required = false)
                   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                   tom: LocalDate?): List<PaaroerendeSykdom> {
        clientValidator.authorizeClient()
        return paaroerendeSykdomService.hentPaaroerendeSykdom(FoedselNr(fodselNr), fom, tom)
    }
}