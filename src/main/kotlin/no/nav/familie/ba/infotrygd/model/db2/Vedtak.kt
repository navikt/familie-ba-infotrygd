package no.nav.familie.ba.infotrygd.model.db2


import no.nav.familie.ba.infotrygd.model.converters.Char2Converter
import no.nav.familie.ba.infotrygd.model.converters.CharConverter
import org.hibernate.annotations.Cascade
import org.hibernate.annotations.CascadeType
import javax.persistence.*


@Entity
@Table(name = "T_VEDTAK")
data class Vedtak(
    @Id
    @Column(name = "VEDTAK_ID", columnDefinition = "DECIMAL")
    val vedtakId: Long,

    @Column(name = "STONAD_ID", columnDefinition = "DECIMAL")
    val stønadId: Long,

    @Column(name = "SAKSNR", columnDefinition = "DECIMAL")
    val saksnummer: Long,

    @Column(name = "SAKSBLOKK", columnDefinition = "CHAR")
    @Convert(converter = CharConverter::class)
    val saksblokk: String,

    @Column(name = "PERSON_LOPENR", columnDefinition = "DECIMAL")
    val løpenummer: Long,

    @Column(name = "KODE_RUTINE", columnDefinition = "CHAR")
    @Convert(converter = Char2Converter::class)
    val kodeRutine: String,

    @Column(name = "KODE_RESULTAT", columnDefinition = "CHAR")
    @Convert(converter = Char2Converter::class)
    val kodeResultat: String,

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "VEDTAK_ID", referencedColumnName = "VEDTAK_ID")
    @Cascade(value = [CascadeType.MERGE])
    val delytelse: List<Delytelse>,
)