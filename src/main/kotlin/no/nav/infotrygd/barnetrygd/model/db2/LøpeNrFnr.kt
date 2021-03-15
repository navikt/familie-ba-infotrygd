package no.nav.infotrygd.barnetrygd.model.db2

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.infotrygd.barnetrygd.model.converters.FoedselNrConverter
import java.io.Serializable
import javax.persistence.*


@Entity
@Table(name = "T_LOPENR_FNR")
data class LøpeNrFnr(
    @Id
    @Column(name = "PERSON_LOPENR", columnDefinition = "DECIMAL")
    val personLøpenummer: Long,

    @Column(name = "PERSONNR", columnDefinition = "CHAR")
    val personnummer: String,
): Serializable