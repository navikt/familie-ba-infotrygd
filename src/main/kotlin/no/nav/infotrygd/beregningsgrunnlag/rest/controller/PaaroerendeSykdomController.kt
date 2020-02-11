package no.nav.infotrygd.beregningsgrunnlag.rest.controller

import io.micrometer.core.annotation.Timed
import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.infotrygd.beregningsgrunnlag.dto.VedtakBarnDto
import no.nav.infotrygd.beregningsgrunnlag.dto.PaaroerendeSykdom
import no.nav.infotrygd.beregningsgrunnlag.dto.SakResult
import no.nav.infotrygd.beregningsgrunnlag.service.ClientValidator
import no.nav.infotrygd.beregningsgrunnlag.service.PaaroerendeSykdomGrunnlagService
import no.nav.infotrygd.beregningsgrunnlag.service.PaaroerendeSykdomSakService
import no.nav.infotrygd.beregningsgrunnlag.service.VedtakBarnService
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
    private val vedtakBarnService: VedtakBarnService,
    private val clientValidator: ClientValidator
) {
    @GetMapping(path = ["/paaroerendeSykdom/grunnlag", "/grunnlag"])
    fun paaroerendeSykdom(
                @RequestParam
                fnr: FoedselsNr,

                @RequestParam
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                fom: LocalDate,

                @RequestParam(required = false)
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                tom: LocalDate?): List<PaaroerendeSykdom> {
        clientValidator.authorizeClient()
        return paaroerendeSykdomGrunnlagService.hentPaaroerendeSykdom(fnr, fom, tom)
    }

    @GetMapping(path = ["/paaroerendeSykdom/sak", "/saker"])
    fun hentSak(@RequestParam
                fnr: FoedselsNr,

                @RequestParam
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                fom: LocalDate,

                @RequestParam(required = false)
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                tom: LocalDate?): SakResult {
        clientValidator.authorizeClient()
        return paaroerendeSykdomSakService.hentSak(fnr, fom, tom)
    }

    @GetMapping(path = ["/vedtakBarn"])
    fun finnVedtakBarn(@RequestParam
                       barnFnr: FoedselsNr,

                       @RequestParam
                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                       fom: LocalDate,

                       @RequestParam(required = false)
                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                       tom: LocalDate?): List<VedtakBarnDto> {
        clientValidator.authorizeClient()
        return vedtakBarnService.finnVedtakBarn(barnFnr, fom, tom)
    }
}