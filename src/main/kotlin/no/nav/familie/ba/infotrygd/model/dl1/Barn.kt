package no.nav.familie.ba.infotrygd.model.dl1

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.familie.ba.infotrygd.model.converters.Char2Converter
import no.nav.familie.ba.infotrygd.model.converters.ReversedFoedselNrConverter
import no.nav.familie.ba.infotrygd.model.converters.ReversedLongFoedselNrConverter
import javax.persistence.*
import no.nav.familie.kontrakter.ba.infotrygd.Barn as BarnDto

@Entity
@Table(name = "BA_BARN_10")
data class Barn(
    @Id
    @Column(name = "ID_BA_BARN", columnDefinition = "DECIMAL")
    val id: Long,

    @Column(name = "B01_PERSONKEY", columnDefinition = "DECIMAL")
    val personKey: Long,

    @Column(name = "F_NR", columnDefinition = "VARCHAR2")
    @Convert(converter = ReversedFoedselNrConverter::class)
    val fnr: FoedselsNr,

    @Column(name = "TK_NR", columnDefinition = "VARCHAR2")
    val tkNr: String,

    @Column(name = "REGION", columnDefinition = "CHAR(1 CHAR)")
    val region: String,

    @Column(name = "B10_BARN_FNR", columnDefinition = "DECIMAL")
    @Convert(converter = ReversedLongFoedselNrConverter::class)
    val barnFnr: FoedselsNr,

    @Column(name = "B10_BA_TOM", columnDefinition = "VARCHAR2")
    val barnetrygdTom: String,

    @Column(name = "B10_BA_IVER", columnDefinition = "VARCHAR2")
    val iverksatt: String,

    @Column(name = "B10_BA_VFOM", columnDefinition = "VARCHAR2")
    val virkningFom: String,

    @Column(name = "B10_STONADS_TYPE", columnDefinition = "VARCHAR2")
    @Convert(converter = Char2Converter::class)
    val stønadstype: String?,
)

fun Barn.toBarnDto(): BarnDto {
    return BarnDto(
        barnFnr = barnFnr.asString,
        barnetrygdTom = barnetrygdTom,
    )
}