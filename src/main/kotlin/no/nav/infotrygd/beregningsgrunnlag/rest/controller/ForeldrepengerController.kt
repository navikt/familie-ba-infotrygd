package no.nav.infotrygd.beregningsgrunnlag.rest.controller

import no.nav.infotrygd.beregningsgrunnlag.rest.dto.Foreldrepenger
import no.nav.infotrygd.beregningsgrunnlag.service.ForeldrepengerService
import no.nav.infotrygd.beregningsgrunnlag.values.FodselNr
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
class ForeldrepengerController(private val foreldrepengerService: ForeldrepengerService) {
    @GetMapping(path = ["/foreldrepenger"])
    fun foreldrepenger(@RequestParam
                       fodselNr: String,

                       @RequestParam
                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                       fom: LocalDate,

                       @RequestParam
                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                       tom: LocalDate?) : List<Foreldrepenger> {
        return foreldrepengerService.hentForeldrepenger(FodselNr(fodselNr), fom, tom)
    }
}