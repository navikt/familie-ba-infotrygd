package no.nav.infotrygd.beregningsgrunnlag.model.db2

import org.hibernate.annotations.Cascade
import org.hibernate.annotations.CascadeType
import java.time.LocalDate
import javax.persistence.*

@Entity
@Table(name = "T_VEDTAK")
data class Vedtak(
    @Id
    @Column(name = "VEDTAK_ID", columnDefinition = "DECIMAL")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "STONAD_ID", referencedColumnName = "STONAD_ID")
    @Cascade(CascadeType.ALL)
    val stonad: Stonad,

    @ManyToOne
    @JoinColumn(name = "PERSON_LOPENR", referencedColumnName = "PERSON_LOPENR")
    @Cascade(CascadeType.ALL)
    val person: LopenrFnr,

    @Column(name = "KODE_RUTINE", columnDefinition = "CHAR")
    val kodeRutine: String,

    @Column(name = "DATO_START", columnDefinition = "DATE")
    val datoStart: LocalDate,

    @OneToOne
    @JoinColumn(name = "VEDTAK_ID", referencedColumnName = "VEDTAK_ID")
    @Cascade(CascadeType.ALL)
    val vedtakSpFaBs: VedtakSpFaBs?,

    @OneToMany
    @JoinColumn(name = "VEDTAK_ID", referencedColumnName = "VEDTAK_ID")
    @Cascade(CascadeType.ALL)
    val delytelser: List<Delytelse>
) {
    fun innenforPeriode(fom: LocalDate, tom: LocalDate?): Boolean {
        if(tom != null) {
            require(fom == tom || fom.isBefore(tom)) { "Tom-dato kan ikke være før fom-dato." }
        }

        if(tom != null && tom.isBefore(datoStart)) {
            return false
        }

        if(fom.isAfter(stonad.datoOpphoer)) {
            return false
        }

        return true
    }
}