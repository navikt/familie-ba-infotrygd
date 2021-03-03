package no.nav.infotrygd.barnetrygd.model

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.familie.ba.sak.infotrygd.Stønad as StønadDto
import no.nav.infotrygd.barnetrygd.model.converters.CharConverter
import no.nav.infotrygd.barnetrygd.model.converters.ReversedFoedselNrConverter
import org.hibernate.annotations.Cascade
import org.hibernate.annotations.CascadeType
import java.io.Serializable
import javax.persistence.*

@Entity
@Table(name = "BA_STOENAD_20")
data class Stønad(
    @Id
    @Column(name = "ID_BA_STOENAD", columnDefinition = "DECIMAL")
    val id: Long,

    @Column(name = "B01_PERSONKEY", columnDefinition = "DECIMAL")
    val personKey: Long,

    @Column(name = "B20_SAK_NR", columnDefinition = "VARCHAR2")
    val sakNr: String,

    @Column(name = "B20_BLOKK", columnDefinition = "CHAR(1 CHAR)")
    val saksblokk: String,

    @Column(name = "B20_STATUS", columnDefinition = "VARCHAR2")
    val status: String,

    @Column(name = "B20_TEKSTKODE", columnDefinition = "VARCHAR2")
    val tekstkode: String,

    @Column(name = "F_NR", columnDefinition = "VARCHAR2")
    @Convert(converter = ReversedFoedselNrConverter::class)
    val fnr: FoedselsNr,

    @Column(name = "TK_NR", columnDefinition = "VARCHAR2")
    val tkNr: String,

    @Column(name = "REGION", columnDefinition = "CHAR(1 CHAR)")
    val region: String,

    @Column(name = "B20_OPPHOERT_IVER", columnDefinition = "VARCHAR2")
    val opphørtIver: String,

    @Column(name = "B20_OPPHOERT_VFOM", columnDefinition = "VARCHAR2")
    val opphørtFom: String,

    @Column(name = "B20_OPPHORSGRUNN", columnDefinition = "CHAR")
    @Convert(converter = CharConverter::class)
    val opphørsgrunn: String? = null,

    @Column(name = "B20_IVERFOM_SEQ", columnDefinition = "VARCHAR2")
    val iverksattFom: String,

    @Column(name = "B20_VIRKFOM_SEQ", columnDefinition = "VARCHAR2")
    val virkningFom: String,

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumns(value = [
        JoinColumn(name = "B01_PERSONKEY", referencedColumnName = "B01_PERSONKEY"),
        JoinColumn(name = "REGION", referencedColumnName = "REGION"),
        JoinColumn(name = "B10_BA_IVER", referencedColumnName = "B20_IVERFOM_SEQ"),
        JoinColumn(name = "B10_BA_VFOM", referencedColumnName = "B20_VIRKFOM_SEQ")
    ])
    @Cascade(value = [CascadeType.MERGE])
    val barn: List<Barn> = emptyList(),

) : Serializable

fun Stønad.toStønadDto(): StønadDto {
    return StønadDto(
        stønadId = this.id,
        sakNr = this.sakNr,
        status = status,
        tekstkode = tekstkode,
        iverksattFom = iverksattFom,
        virkningFom = virkningFom,
        opphørtIver = opphørtIver,
        opphørtFom = this.opphørtFom,
        opphørsgrunn = this.opphørsgrunn,
        barn = barn.map { it.toBarnDto() },
    )
}