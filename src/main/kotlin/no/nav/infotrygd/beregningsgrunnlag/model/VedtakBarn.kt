package no.nav.infotrygd.beregningsgrunnlag.model

import java.math.BigDecimal
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "IS_VEDTAK_BARN_18")
data class VedtakBarn(
    @Id
    @Column(name = "ID_VEDBA", nullable = false, columnDefinition = "DECIMAL")
    val id: Long,

    @Column(name = "IS01_PERSONKEY", columnDefinition = "DECIMAL")
    val personKey: Long,

    @Column(name = "IS18_ARBUFOER_SEQ", columnDefinition = "CHAR")
    val arbufoerSeq: String,

    @Column(name = "IS18_KODE", columnDefinition = "CHAR")
    val kode: String,

    @Column(name = "IS18_TIDSK_PROS", columnDefinition = "DECIMAL")
    val dekningsgrad: BigDecimal
)