package no.nav.familie.ba.infotrygd.model.dl1

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.familie.ba.infotrygd.model.converters.CharConverter
import no.nav.familie.ba.infotrygd.model.converters.ReversedFoedselNrConverter
import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

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

    @Column(name = "B20_ANT_BARN", columnDefinition = "DECIMAL")
    val antallBarn: Int,
) : Serializable


fun Stønad.tilTrunkertStønad(): TrunkertStønad {
    return TrunkertStønad(
        id = id,
        personKey = personKey,
        fnr = fnr,
        sakNr = sakNr,
        saksblokk = saksblokk,
        status = status,
        region = region,
        virkningFom = virkningFom,
        opphørtFom = opphørtFom,
        iverksattFom = iverksattFom,
        antallBarn = antallBarn
    )
}


data class TrunkertStønad(
    val id: Long,

    val personKey: Long,

    val fnr: FoedselsNr?,

    val sakNr: String,

    val saksblokk: String,

    val status: String,

    val region: String,

    val virkningFom: String,

    val opphørtFom: String,

    val iverksattFom: String,

    val antallBarn: Int,

    val pensjonstrygdet: String? = "",
)

