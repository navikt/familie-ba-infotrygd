package no.nav.infotrygd.beregningsgrunnlag.service

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.infotrygd.beregningsgrunnlag.dto.PaaroerendeSykdom
import no.nav.infotrygd.beregningsgrunnlag.dto.vedtakToPaaroerendeSykdom
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
            .map { vedtakToPaaroerendeSykdom(it) }
        return (isbase + barnsSykdom).sortedBy { it.identdato }
    }
}