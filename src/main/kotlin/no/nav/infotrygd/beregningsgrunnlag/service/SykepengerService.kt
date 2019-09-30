package no.nav.infotrygd.beregningsgrunnlag.service

import no.nav.infotrygd.beregningsgrunnlag.repository.PeriodeRepository
import no.nav.infotrygd.beregningsgrunnlag.dto.Grunnlag
import org.springframework.stereotype.Service

@Service
class SykepengerService(private val periodeRepository: PeriodeRepository) {
    fun hentSykepenger(): List<Grunnlag> {
        return listOf()
    }
}