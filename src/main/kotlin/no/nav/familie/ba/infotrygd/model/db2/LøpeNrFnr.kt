package no.nav.familie.ba.infotrygd.model.db2

import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table


@Entity
@Table(name = "T_LOPENR_FNR")
data class LøpeNrFnr(
    @Id
    @Column(name = "PERSON_LOPENR", columnDefinition = "DECIMAL")
    val personLøpenummer: Long,

    @Column(name = "PERSONNR", columnDefinition = "CHAR")
    val personnummer: String,
): Serializable