package no.nav.infotrygd.beregningsgrunnlag.model.db2

import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Arbeidskategori
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "T_VEDTAK_SP_FA_BS")
data class VedtakSpFaBs (
    @Id
    @Column(name = "VEDTAK_ID", columnDefinition = "DECIMAL")
    val vedtakId: Long,

    @Column(name = "ARBKAT", columnDefinition = "CHAR")
    val arbeidskategori: Arbeidskategori
)