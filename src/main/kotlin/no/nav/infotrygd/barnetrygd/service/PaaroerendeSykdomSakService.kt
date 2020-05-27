package no.nav.infotrygd.barnetrygd.service

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.infotrygd.barnetrygd.dto.*
import no.nav.infotrygd.barnetrygd.repository.SakRepository
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class PaaroerendeSykdomSakService(
    private val paaroerendeSykdomISBasenService: PaaroerendeSykdomISBasenService,
    private val paaroerendeSykdomVedtaksbasenService: PaaroerendeSykdomVedtaksbasenService,
    private val sakRepository: SakRepository
) {
    fun hentSak(fnr: FoedselsNr, fom: LocalDate, tom: LocalDate?): SakResult {
        val sakRes = sakRepository.findPaaroerendeSykdomByFnr(fnr)
            .filter { it.innenforPeriode(fom, tom) }
            .map { sakToSakDto(it) }
        
        val isRes = paaroerendeSykdomISBasenService.hentPaaroerendeSykdom(fnr)
            .filter { it.innenforPeriode(fom, tom) }
            .map { periodeToSakDto(it) }
        val vedtakRes = paaroerendeSykdomVedtaksbasenService.barnsSykdom(fnr)
            .filter { it.innenforPeriode(fom, tom) }
            .map { vedtakToSakDto(it) }

        return SakResult(
            saker = sakRes.sortedBy { it.iverksatt },
            vedtak = (isRes + vedtakRes).sortedBy { it.iverksatt }
        )
    }
}