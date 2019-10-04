package no.nav.infotrygd.beregningsgrunnlag.model.db2

import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Stoenadstype
import org.hibernate.annotations.Cascade
import org.hibernate.annotations.CascadeType
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "T_DELYTELSE")
@IdClass(DelytelseId::class)
data class Delytelse(
    @Id
    @Column(name = "VEDTAK_ID", nullable = false, columnDefinition = "DECIMAL")
    val vedtakId: Long,

    @Id
    @Column(name = "TYPE_DELYTELSE", nullable = false, columnDefinition = "CHAR")
    val type: Stoenadstype,

    @Id
    @Column(name = "TIDSPUNKT_REG", nullable = false, columnDefinition = "TIMESTAMP")
    val tidspunktRegistrert: LocalDateTime,

    @Column(name = "FOM", columnDefinition = "DATE")
    val fom: LocalDate,

    @Column(name = "TOM", columnDefinition = "DATE")
    val tom: LocalDate,

    @OneToOne
    @JoinColumns(value = [
        JoinColumn(name = "VEDTAK_ID", referencedColumnName = "VEDTAK_ID"),
        JoinColumn(name = "TYPE_DELYTELSE", referencedColumnName = "TYPE_DELYTELSE"),
        JoinColumn(name = "TIDSPUNKT_REG", referencedColumnName = "TIDSPUNKT_REG")
    ])
    @Cascade(CascadeType.ALL)
    val delytelseSpFaBs: DelytelseSpFaBs?
)
