package no.nav.infotrygd.beregningsgrunnlag.service

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.infotrygd.beregningsgrunnlag.dto.PaaroerendeSykdom
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class PaaroerendeSykdomService(
    private val paaroerendeSykdomISBasenService: PaaroerendeSykdomISBasenService,
    private val barnSykdomService: BarnSykdomService
) {
    fun hentPaaroerendeSykdom(foedselsNr: FoedselsNr, fom: LocalDate, tom: LocalDate?): List<PaaroerendeSykdom> {
        val isbase = paaroerendeSykdomISBasenService.hentPaaroerendeSykdom(foedselsNr, fom, tom)
        val barnsSykdom = barnSykdomService.barnsSykdom(foedselsNr, fom, tom)
        return (isbase + barnsSykdom).sortedBy { it.identdato }
    }
}