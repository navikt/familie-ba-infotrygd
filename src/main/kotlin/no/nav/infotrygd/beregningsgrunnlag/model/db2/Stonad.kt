package no.nav.infotrygd.beregningsgrunnlag.model.db2

import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.KodeRutine
import org.hibernate.annotations.Cascade
import org.hibernate.annotations.CascadeType
import java.time.LocalDate
import javax.persistence.*

@Entity
@Table(name = "T_STONAD")
data class Stonad(
    @Id
    @Column(name = "STONAD_ID", columnDefinition = "DECIMAL")
    val id: Long,

    @Column(name = "KODE_RUTINE", columnDefinition = "CHAR")
    val kodeRutine: KodeRutine,

    @Column(name = "DATO_START", columnDefinition = "DATE")
    val datoStart: LocalDate,

    @Column(name = "DATO_OPPHOR", columnDefinition = "DATE")
    val datoOpphoer: LocalDate,

    @OneToOne
    @JoinColumn(name = "STONAD_ID", referencedColumnName = "STONAD_ID")
    @Cascade(CascadeType.ALL)
    val stonadBs: StonadBs?
)