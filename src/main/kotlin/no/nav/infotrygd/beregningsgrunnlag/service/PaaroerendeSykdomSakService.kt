package no.nav.infotrygd.beregningsgrunnlag.service

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.infotrygd.beregningsgrunnlag.dto.*
import no.nav.infotrygd.beregningsgrunnlag.repository.SakRepository
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class PaaroerendeSykdomSakService(
    private val paaroerendeSykdomISBasenService: PaaroerendeSykdomISBasenService,
    private val paaroerendeSykdomVedtaksbasenService: PaaroerendeSykdomVedtaksbasenService,
    private val sakRepository: SakRepository
) {
    fun hentSak(fnr: FoedselsNr, fom: LocalDate, tom: LocalDate?): List<SakDto> {
        val isRes = paaroerendeSykdomISBasenService.hentPaaroerendeSykdom(fnr)
            .filter { it.innenforPeriode(fom, tom) }
            .map { periodeToSakDto(it) }
        val vedtakRes = paaroerendeSykdomVedtaksbasenService.barnsSykdom(fnr)
            .filter { it.innenforPeriode(fom, tom) }
            .map { vedtakToSakDto(it) }
        val sakRes = sakRepository.findPaaroerendeSykdomByFnr(fnr)
            .filter { it.innenforPeriode(fom, tom) }
            .map { sakToSakDto(it) }

        return (isRes + vedtakRes + sakRes).sortedBy { it.iverksatt }
    }
}