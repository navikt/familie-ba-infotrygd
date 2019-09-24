package no.nav.infotrygd.beregningsgrunnlag.service

import no.nav.infotrygd.beregningsgrunnlag.repository.PeriodeRepository
import no.nav.infotrygd.beregningsgrunnlag.rest.dto.Grunnlag
import org.springframework.stereotype.Service

@Service
class PaaroerendeSykdomService(val periodeRepository: PeriodeRepository) {
    fun hentPaaroerendeSykdom(): List<Grunnlag> {
        return listOf()
    }
}