package no.nav.infotrygd.beregningsgrunnlag.rest.controller

import io.micrometer.core.annotation.Timed
import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.infotrygd.beregningsgrunnlag.dto.PaaroerendeSykdom
import no.nav.infotrygd.beregningsgrunnlag.dto.SakDto
import no.nav.infotrygd.beregningsgrunnlag.service.ClientValidator
import no.nav.infotrygd.beregningsgrunnlag.service.PaaroerendeSykdomGrunnlagService
import no.nav.infotrygd.beregningsgrunnlag.service.PaaroerendeSykdomSakService
import no.nav.security.oidc.api.Protected
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@Protected
@RestController
@Timed(value = "infotrygd_grunnlag_paaroerendeSykdom_controller", percentiles = [0.5, 0.95])
class PaaroerendeSykdomController(
    private val paaroerendeSykdomGrunnlagService: PaaroerendeSykdomGrunnlagService,
    private val paaroerendeSykdomSakService: PaaroerendeSykdomSakService,
    private val clientValidator: ClientValidator
) {
    @GetMapping(path = ["/paaroerendeSykdom/grunnlag"])
    fun paaroerendeSykdom(@RequestParam
                   fodselNr: FoedselsNr,

                   @RequestParam
                   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                   fom: LocalDate,

                   @RequestParam(required = false)
                   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                   tom: LocalDate?): List<PaaroerendeSykdom> {
        clientValidator.authorizeClient()
        return paaroerendeSykdomGrunnlagService.hentPaaroerendeSykdom(fodselNr, fom, tom)
    }

    @GetMapping(path = ["/paaroerendeSykdom/sak"])
    fun hentSak(@RequestParam
                fodselNr: FoedselsNr,

                @RequestParam
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                fom: LocalDate,

                @RequestParam(required = false)
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                tom: LocalDate?): List<SakDto> {
        clientValidator.authorizeClient()
        return paaroerendeSykdomSakService.hentSak(fodselNr, fom, tom)
    }
}