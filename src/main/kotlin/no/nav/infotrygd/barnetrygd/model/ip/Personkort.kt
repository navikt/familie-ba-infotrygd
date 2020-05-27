package no.nav.infotrygd.barnetrygd.model.ip

import no.nav.infotrygd.barnetrygd.model.converters.NavLocalDateConverter
import org.hibernate.annotations.Cascade
import org.hibernate.annotations.CascadeType
import java.time.LocalDate
import javax.persistence.*

@Entity
@Table(name = "IP_PERSONKORT_90")
data class Personkort(
    @Id
    @Column(name = "ID_PERSK", columnDefinition = "DECIMAL")
    val id: Long,

    @JoinColumn(name = "IP01_PERSNKEY", referencedColumnName = "IP01_PERSNKEY", columnDefinition = "VARCHAR2")
    @ManyToOne
    @Cascade(CascadeType.ALL)
    val person: Person,

    @Column(name = "IP90_DATO_SEQ", columnDefinition = "DECIMAL")
    val datoSeq: Long,

    @Column(name = "IP90_KONTONR", columnDefinition = "DECIMAL")
    val kontonummer: Long,

    @Column(name = "IP90_DATO", columnDefinition = "DECIMAL")
    @Convert(converter = NavLocalDateConverter::class)
    val dato: LocalDate,

    @Column(name = "IP90_FOM", columnDefinition = "DECIMAL")
    @Convert(converter = NavLocalDateConverter::class)
    val fom: LocalDate,

    @Column(name = "IP90_TOM", columnDefinition = "DECIMAL")
    @Convert(converter = NavLocalDateConverter::class)
    val tom: LocalDate?,

    @Column(name = "IP90_TEKST", columnDefinition = "VARCHAR2")
    val tekst: String
) {
    fun innenforPeriode(fom: LocalDate, tom: LocalDate): Boolean {
        require(fom <= tom) { "Tom-dato kan ikke være før fom-dato" }

        if(this.dato in fom..tom) {
            return true
        }

        if(this.fom in fom..tom) {
            return true
        }

        if(this.tom != null && this.tom!! in fom..tom) {
            return true
        }

        if(fom in this.fom..(this.tom ?: LocalDate.MAX)
            || tom in this.fom..(this.tom ?: LocalDate.MAX)) {
            return true
        }

        return false
    }
}