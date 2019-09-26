package no.nav.infotrygd.beregningsgrunnlag.repository

import no.nav.infotrygd.beregningsgrunnlag.model.VedtakBarn
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface VedtakBarnRepository : JpaRepository<VedtakBarn, Long> {
    fun findByPersonKeyAndArbufoerSeqAndKode(personKey: Long, arbufoerSeq: String, kode: String): VedtakBarn?
}