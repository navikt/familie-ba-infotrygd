package no.nav.infotrygd.beregningsgrunnlag.service

import no.nav.infotrygd.beregningsgrunnlag.dto.PaaroerendeSykdom
import no.nav.infotrygd.beregningsgrunnlag.values.FoedselNr
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class PaaroerendeSykdomService(
    private val paaroerendeSykdomISBasenService: PaaroerendeSykdomISBasenService,
    private val barnSykdomService: BarnSykdomService
) {
    fun hentPaaroerendeSykdom(foedselNr: FoedselNr, fom: LocalDate, tom: LocalDate?): List<PaaroerendeSykdom> {
        val isbase = paaroerendeSykdomISBasenService.hentPaaroerendeSykdom(foedselNr, fom, tom)
        val barnsSykdom = barnSykdomService.barnsSykdom(foedselNr, fom, tom)
        return (isbase + barnsSykdom).sortedBy { it.identdato }
    }
}