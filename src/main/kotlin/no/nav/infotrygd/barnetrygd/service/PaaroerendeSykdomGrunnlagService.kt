package no.nav.infotrygd.barnetrygd.service

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.infotrygd.barnetrygd.dto.PaaroerendeSykdom
import no.nav.infotrygd.barnetrygd.dto.vedtakToPaaroerendeSykdom
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class PaaroerendeSykdomGrunnlagService(
    private val paaroerendeSykdomISBasenService: PaaroerendeSykdomISBasenService,
    private val paaroerendeSykdomVedtaksbasenService: PaaroerendeSykdomVedtaksbasenService
) {
    fun hentPaaroerendeSykdom(foedselsNr: FoedselsNr, fom: LocalDate, tom: LocalDate?): List<PaaroerendeSykdom> {
        val isbase = paaroerendeSykdomISBasenService.hentPaaroerendeSykdom(foedselsNr)
            .filter { it.innenforPeriode(fom, tom) }
            .map { periodeToPaaroerendeSykdom(it) }
        val barnsSykdom = paaroerendeSykdomVedtaksbasenService.barnsSykdom(foedselsNr)
            .filter { it.innenforPeriode(fom, tom) }
            .filter { !it.annullert }
            .map { vedtakToPaaroerendeSykdom(it) }
        return (isbase + barnsSykdom).sortedBy { it.identdato }
    }
}