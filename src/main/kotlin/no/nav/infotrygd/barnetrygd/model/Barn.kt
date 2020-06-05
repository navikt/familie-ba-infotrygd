package no.nav.infotrygd.barnetrygd.model

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.infotrygd.barnetrygd.model.converters.ReversedFoedselNrConverter
import no.nav.infotrygd.barnetrygd.model.converters.ReversedLongFoedselNrConverter
import javax.persistence.*

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

    @Column(name = "B10_BARN_FNR", columnDefinition = "DECIMAL")
    @Convert(converter = ReversedLongFoedselNrConverter::class)
    val barnFnr: FoedselsNr,

    @Column(name = "B10_BA_TOM", columnDefinition = "VARCHAR2")
    val barnetrygdTom: String
)