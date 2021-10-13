package no.nav.familie.ba.infotrygd.model.dl1

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.familie.ba.infotrygd.model.converters.ReversedFoedselNrConverter
import javax.persistence.*
import java.io.Serializable

@Entity
@Table(name = "BA_PERSON_01")
data class Person(
    @Id
    @Column(name = "ID_BA_PERS", columnDefinition = "DECIMAL")
    val id: Long,

    @Column(name = "B01_PERSONKEY", columnDefinition = "DECIMAL")
    val personKey: Long,

    @Column(name = "F_NR", columnDefinition = "VARCHAR2")
    @Convert(converter = ReversedFoedselNrConverter::class)
    val fnr: FoedselsNr,

    @Column(name = "TK_NR", columnDefinition = "VARCHAR2")
    val tkNr: String,

    @Column(name = "REGION", columnDefinition = "CHAR(1 CHAR)")
    val region: String
): Serializable