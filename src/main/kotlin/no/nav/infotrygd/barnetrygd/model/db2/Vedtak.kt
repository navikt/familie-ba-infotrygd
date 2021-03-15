package no.nav.infotrygd.barnetrygd.model.db2


import no.nav.infotrygd.barnetrygd.model.converters.CharConverter
import org.hibernate.annotations.Cascade
import org.hibernate.annotations.CascadeType
import javax.persistence.*


@Entity
@Table(name = "T_VEDTAK")
data class Vedtak(
    @Id
    @Column(name = "VEDTAK_ID", columnDefinition = "DECIMAL")
    val vedtakId: Long,

    @Column(name = "SAKSNR", columnDefinition = "DECIMAL")
    val saksnummer: Long,

    @Column(name = "SAKSBLOKK", columnDefinition = "CHAR")
    @Convert(converter = CharConverter::class)
    val saksblokk: String,

    @Column(name = "PERSON_LOPENR", columnDefinition = "DECIMAL")
    val løpenummer: Long,

    @OneToOne
    @JoinColumn(name = "VEDTAK_ID", referencedColumnName = "VEDTAK_ID")
    @Cascade(value = [CascadeType.MERGE])
    val delytelse: Delytelse,
)