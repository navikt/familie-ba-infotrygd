package no.nav.infotrygd.barnetrygd.model.db2

import no.nav.infotrygd.barnetrygd.model.converters.BrukerIdConverter
import org.hibernate.annotations.Cascade
import org.hibernate.annotations.CascadeType
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "T_STONAD_BS")
data class StonadBs(
    @Id
    @Column(name = "STONAD_ID", columnDefinition = "DECIMAL")
    val id: Long,

    @Column(name = "BRUKERID", columnDefinition = "CHAR")
    @Convert(converter = BrukerIdConverter::class)
    val brukerId: String,

    @ManyToOne
    @JoinColumn(name = "LOPENR_BARN", referencedColumnName = "PERSON_LOPENR")
    @Cascade(CascadeType.ALL)
    val barn: LopenrFnr,

    @Column(name = "TIDSPUNKT_REG", columnDefinition = "TIMESTAMP")
    val tidspunktRegistrert: LocalDateTime
)