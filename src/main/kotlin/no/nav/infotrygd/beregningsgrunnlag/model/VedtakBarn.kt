package no.nav.infotrygd.beregningsgrunnlag.model

import no.nav.infotrygd.beregningsgrunnlag.model.converters.UtbetalingsgradConverter
import javax.persistence.*

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

    @Column(name = "IS18_DEKNINGSGRAD", columnDefinition = "CHAR")
    @Convert(converter = UtbetalingsgradConverter::class)
    val dekningsgrad: Int
)