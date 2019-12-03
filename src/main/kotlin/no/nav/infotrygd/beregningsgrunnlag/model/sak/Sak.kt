package no.nav.infotrygd.beregningsgrunnlag.model.sak

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.infotrygd.beregningsgrunnlag.model.converters.*
import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.SakResultat
import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.SakStatus
import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.SakType
import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.SakValg
import org.hibernate.annotations.Cascade
import org.hibernate.annotations.CascadeType
import java.io.Serializable
import java.time.LocalDate
import javax.persistence.*

@Entity
@Table(name = "SA_SAK_10")
data class Sak(
    @Id
    @Column(name = "ID_SAK", columnDefinition = "DECIMAL", nullable = false)
    var id: Long,

    @Column(name = "F_NR", columnDefinition = "CHAR")
    @Convert(converter = ReversedFoedselNrConverter::class)
    val fnr: FoedselsNr,

    @Column(name = "S01_PERSONKEY", columnDefinition = "DECIMAL")
    val personKey: Long,

    @Column(name = "S05_SAKSBLOKK", columnDefinition = "CHAR")
    val saksblokk: String,

    @Column(name = "S10_SAKSNR", columnDefinition = "CHAR")
    val saksnummer: String,

    @Column(name = "S10_KAPITTELNR", columnDefinition = "CHAR")
    val kapittelNr: String,

    @Column(name = "S10_VALG", columnDefinition = "CHAR")
    @Convert(converter = SakValgConverter::class)
    val valg: SakValg,

    @Column(name = "S10_TYPE", columnDefinition = "CHAR")
    @Convert(converter = SakTypeConverter::class)
    val type: SakType,

    @Column(name = "S10_RESULTAT", columnDefinition = "CHAR")
    @Convert(converter = SakResultatConverter::class)
    val resultat: SakResultat,

    /** INFO: NavReversedLocalDateConverter lar seg ikke sortere i databasen! */
    @Column(name = "S10_VEDTAKSDATO", columnDefinition = "DECIMAL")
    @Convert(converter = NavReversedLocalDateConverter::class)
    val vedtaksdato: LocalDate?,

    /** INFO: NavReversedLocalDateConverter lar seg ikke sortere i databasen! */
    @Column(name = "S10_IVERKSATTDATO", columnDefinition = "DECIMAL")
    @Convert(converter = NavReversedLocalDateConverter::class)
    val iverksattdato: LocalDate?,

    @Column(name = "S10_REG_DATO", columnDefinition = "DECIMAL")
    @Convert(converter = NavReversedLocalDateConverter::class)
    val registrert: LocalDate?,

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumns(value = [
        JoinColumn(name = "S01_PERSONKEY", referencedColumnName = "S01_PERSONKEY"),
        JoinColumn(name = "S05_SAKSBLOKK", referencedColumnName = "S05_SAKSBLOKK"),
        JoinColumn(name = "S10_SAKSNR", referencedColumnName = "S10_SAKSNR")
    ])
    @Cascade(value = [CascadeType.ALL])
    val statushistorikk: List<Status>
) : Serializable {
    val status: SakStatus
        get() = statushistorikk.minBy { it.lopeNr }?.status ?: SakStatus.IKKE_BEHANDLET

    fun innenforPeriode(fom: LocalDate, tom: LocalDate?): Boolean {
        if(tom != null) {
            require(fom == tom || fom.isBefore(tom)) { "Tom-dato kan ikke være før fom-dato." }
        }

        if(tom != null && tom.isBefore(registrert)) {
            return false
        }

        if(fom.isAfter(registrert)) {
            return false
        }

        return true
    }
}
