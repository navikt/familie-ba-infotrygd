package no.nav.infotrygd.barnetrygd.rest.controller

import io.micrometer.core.annotation.Timed
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.infotrygd.barnetrygd.dto.VedtakPleietrengendeDto
import no.nav.infotrygd.barnetrygd.dto.PaaroerendeSykdom
import no.nav.infotrygd.barnetrygd.dto.RammevedtakDto
import no.nav.infotrygd.barnetrygd.dto.SakResult
import no.nav.infotrygd.barnetrygd.service.*
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
    private val vedtakPleietrengendeService: VedtakPleietrengendeService,
    private val rammevedtakService: RammevedtakService,
    private val clientValidator: ClientValidator
) {
    @ApiOperation("Finner beregningsgrunnlag basert på fødselsnummeret til søker.")
    @GetMapping(path = ["/paaroerendeSykdom/grunnlag", "/grunnlag"])
    fun paaroerendeSykdom(
                @ApiParam(
                    value = "Søkers fødselsnummer",
                    required = true)
                @RequestParam
                fnr: FoedselsNr,

                @ApiParam(
                    value = "Fra-dato for søket. Matcher vedtaksperiode for vedtak eller registrertdato for saker.",
                    example = "2019-01-01",
                    required = true)
                @RequestParam
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                fom: LocalDate,

                @ApiParam(
                    value = "Til-dato for søket. Matcher vedtaksperiode for vedtak eller registrertdato for saker.",
                    example = "2019-01-01")
                @RequestParam(required = false)
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                tom: LocalDate?): List<PaaroerendeSykdom> {
        clientValidator.authorizeClient()
        return paaroerendeSykdomGrunnlagService.hentPaaroerendeSykdom(fnr, fom, tom)
    }

    @ApiOperation("Finner saker og vedtak basert på fødselsnummeret til søker.")
    @GetMapping(path = ["/paaroerendeSykdom/sak", "/saker"])
    fun hentSak(@ApiParam(
                    value = "Søkers fødselsnummer",
                    required = true)
                @RequestParam
                fnr: FoedselsNr,

                @ApiParam(
                    value = "Fra-dato for søket. Matcher vedtaksperiode for vedtak eller registrertdato for saker.",
                    example = "2019-01-01",
                    required = true)
                @RequestParam
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                fom: LocalDate,

                @ApiParam(
                    value = "Til-dato for søket. Matcher vedtaksperiode for vedtak eller registrertdato for saker.",
                    example = "2019-01-01")
                @RequestParam(required = false)
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                tom: LocalDate?): SakResult {
        clientValidator.authorizeClient()
        return paaroerendeSykdomSakService.hentSak(fnr, fom, tom)
    }

    @ApiOperation("Finner vedtak basert på fødselsnummeret til pleietrengende.")
    @GetMapping(path = ["/vedtakForPleietrengende"])
    fun finnVedtakForPleietrengende(@ApiParam(
                           value ="Pleietrengendes fødselsnummer",
                           required = true)
                       @RequestParam
                       fnr: FoedselsNr,

                                    @ApiParam(
                           value = "Fra-dato for søket. Matcher vedtaksperiode for vedtak eller registrertdato for saker.",
                           example = "2019-01-01",
                           required = true)
                       @RequestParam
                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                       fom: LocalDate,

                                    @ApiParam(
                           value = "Til-dato for søket. Matcher vedtaksperiode for vedtak eller registrertdato for saker.",
                           example = "2019-01-01")
                       @RequestParam(required = false)
                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                       tom: LocalDate?): List<VedtakPleietrengendeDto> {
        clientValidator.authorizeClient()
        return vedtakPleietrengendeService.finnVedtakForPleietrengende(fnr, fom, tom)
    }

    @ApiOperation("Finner rammevedtak basert på fødselsnummeret til søker.")
    @GetMapping(path = ["/rammevedtak/omsorgspenger"])
    fun finnRammevedtakForOmsorgspenger(
                        @ApiParam(
                            value = "Søkers fødselsnummer",
                            required = true)
                        @RequestParam
                        fnr: FoedselsNr,

                        @ApiParam(
                            value = "Fra-dato for søket. Matcher vedtaksperiode eller dato for rammevedtak.",
                            example = "2019-01-01",
                            required = true)
                        @RequestParam
                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                        fom: LocalDate,

                        @ApiParam(
                            value = "Til-dato for søket. Matcher vedtaksperiode eller dato for rammevedtak.",
                            example = "2019-01-01")
                        @RequestParam(required = false)
                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                        tom: LocalDate?): List<RammevedtakDto> {
        clientValidator.authorizeClient()
        return rammevedtakService.hentRammevedtak(RammevedtakService.KONTONUMMER_OM, fnr, fom, tom)
    }
}