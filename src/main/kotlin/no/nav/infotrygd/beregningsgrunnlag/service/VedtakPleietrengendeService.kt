package no.nav.infotrygd.beregningsgrunnlag.service

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.infotrygd.beregningsgrunnlag.dto.SakDto
import no.nav.infotrygd.beregningsgrunnlag.dto.VedtakPleietrengendeDto
import no.nav.infotrygd.beregningsgrunnlag.dto.periodeToSakDto
import no.nav.infotrygd.beregningsgrunnlag.dto.vedtakToSakDto
import no.nav.infotrygd.beregningsgrunnlag.repository.PeriodeRepository
import no.nav.infotrygd.beregningsgrunnlag.repository.VedtakRepository
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class VedtakPleietrengendeService(
    private val periodeRepository: PeriodeRepository,
    private val vedtakRepository: VedtakRepository
) {
    fun finnVedtakForPleietrengende(barnFnr: FoedselsNr, fom: LocalDate, tom: LocalDate?): List<VedtakPleietrengendeDto> {

        val vedtakByFnr = mutableMapOf<FoedselsNr, MutableList<SakDto>>()

        periodeRepository.findByBarnFnr(barnFnr)
            .filter { it.innenforPeriode(fom, tom) }
            .groupByTo(vedtakByFnr, {it.fnr}, ::periodeToSakDto)

        vedtakRepository.findByBarnFnr(barnFnr)
            .filter { it.innenforPeriode(fom, tom) }
            .groupByTo(vedtakByFnr, {it.person.fnr}, ::vedtakToSakDto)

        return vedtakByFnr.map { entry ->
            VedtakPleietrengendeDto(
                soekerFnr = entry.key.asString,
                vedtak = entry.value.sortedByDescending { it.iverksatt })
        }.sortedBy { it.soekerFnr }
    }
}