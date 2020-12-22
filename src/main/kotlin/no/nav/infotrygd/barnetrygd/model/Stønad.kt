package no.nav.infotrygd.barnetrygd.model

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.infotrygd.barnetrygd.model.converters.CharConverter
import no.nav.infotrygd.barnetrygd.model.converters.ReversedFoedselNrConverter
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

    @Column(name = "F_NR", columnDefinition = "VARCHAR2")
    @Convert(converter = ReversedFoedselNrConverter::class)
    val fnr: FoedselsNr,

    @Column(name = "TK_NR", columnDefinition = "VARCHAR2")
    val tkNr: String,

    @Column(name = "REGION", columnDefinition = "CHAR(1 CHAR)")
    val region: String,

    @Column(name = "B20_OPPHOERT_VFOM", columnDefinition = "VARCHAR2")
    val opphørtFom: String,

    @Column(name = "B20_OPPHORSGRUNN", columnDefinition = "CHAR")
    @Convert(converter = CharConverter::class)
    val opphørsgrunn: String? = null,

): Serializable