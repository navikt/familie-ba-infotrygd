package no.nav.infotrygd.beregningsgrunnlag.model.db2

import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Arbeidskategori
import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Stoenadstype
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "T_DELYTELSE_SP_FA_BS")
@IdClass(DelytelseId::class)
data class DelytelseSpFaBs(
    @Id
    @Column(name = "VEDTAK_ID", columnDefinition = "DECIMAL")
    val vedtakId: Long,

    @Id
    @Column(name = "TYPE_DELYTELSE", columnDefinition = "CHAR")
    val type: Stoenadstype,

    @Id
    @Column(name = "TIDSPUNKT_REG", columnDefinition = "TIMESTAMP")
    val tidspunktRegistrert: LocalDateTime,

    @Column(name = "GRAD", columnDefinition = "DECIMAL")
    val grad: Int

//    @Column(name = "")
//    val arbeidskategori: Arbeidskategori // TODO
)