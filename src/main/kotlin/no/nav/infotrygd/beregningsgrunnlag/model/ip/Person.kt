package no.nav.infotrygd.beregningsgrunnlag.model.ip

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.infotrygd.beregningsgrunnlag.model.converters.ReversedFoedselNrConverter
import java.io.Serializable
import javax.persistence.*

@Entity
@Table(name = "IP_PERSON_01")
data class Person(
    @Id
    @Column(name = "ID_IPERS", columnDefinition = "DECIMAL")
    val id: Long,

    @Column(name = "IP01_PERSNKEY", columnDefinition = "VARCHAR2")
    val merkeligPersonKey: String,

    @Column(name = "F_NR", columnDefinition = "VARCHAR2")
    @Convert(converter = ReversedFoedselNrConverter::class)
    val fnr: FoedselsNr
) : Serializable