package no.nav.infotrygd.beregningsgrunnlag.model

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.infotrygd.beregningsgrunnlag.model.converters.*
import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Arbeidskategori
import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Frisk
import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Stoenadstype
import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Tema
import org.hibernate.annotations.Cascade
import org.hibernate.annotations.CascadeType
import java.io.Serializable
import java.time.LocalDate
import javax.persistence.*

@Entity
@Table(name = "IS_PERIODE_10")
data class Periode(
    @Id
    @Column(name = "ID_PERI10", nullable = false, columnDefinition = "DECIMAL")
    val id: Long,

    @Column(name = "IS01_PERSONKEY", columnDefinition = "DECIMAL")
    val personKey: Long,

    @Column(name = "IS10_ARBUFOER_SEQ", columnDefinition = "DECIMAL")
    val arbufoerSeq: Long,

    @Column(name = "IS10_STOENADS_TYPE", columnDefinition = "CHAR")
    val stoenadstype: Stoenadstype?,

    @Column(name = "F_NR", columnDefinition = "CHAR")
    @Convert(converter = ReversedFoedselNrConverter::class)
    val fnr: FoedselsNr,

    @Column(name = "IS10_FRISK", columnDefinition = "CHAR")
    @Convert(converter = FriskConverter::class)
    val frisk: Frisk,

    @Column(name = "IS10_UTBET_FOM", columnDefinition = "DECIMAL")
    @Convert(converter = NavLocalDateConverter::class)
    val utbetaltFom: LocalDate?,

    @Column(name = "IS10_UTBET_TOM", columnDefinition = "DECIMAL")
    @Convert(converter = NavLocalDateConverter::class)
    val utbetaltTom: LocalDate?,

    @Column(name = "IS10_ARBUFOER", columnDefinition = "DECIMAL")
    @Convert(converter = NavLocalDateConverter::class)
    val arbufoer: LocalDate,

    @Column(name = "IS10_ARBUFOER_TOM", columnDefinition = "DECIMAL")
    @Convert(converter = NavLocalDateConverter::class)
    val arbufoerTom: LocalDate?,

    @Column(name = "IS10_FDATO", columnDefinition = "DECIMAL")
    @Convert(converter = NavLocalDateConverter::class)
    val foedselsdatoBarn: LocalDate?,

    @Column(name = "IS10_MORFNR", columnDefinition = "DECIMAL")
    @Convert(converter = ReversedLongFoedselNrConverter::class)
    val morFnr: FoedselsNr?,

    @Column(name = "IS10_ARBKAT", columnDefinition = "CHAR")
    val arbeidskategori: Arbeidskategori?,

    @Column(name = "IS10_REG_DATO", columnDefinition = "CHAR")
    @Convert(converter = NavCharDateConverter::class)
    val registrert: LocalDate?,

    @Column(name = "IS10_BRUKERID", columnDefinition = "CHAR")
    @Convert(converter = BrukerIdConverter::class)
    val brukerId: String?,

    @Column(name = "IS10_STOPPDATO", columnDefinition = "DECIMAL")
    @Convert(converter = NavLocalDateConverter::class)
    val stoppdato: LocalDate?,

    @Column(name = "IS10_FRISKM_DATO", columnDefinition = "DECIMAL")
    @Convert(converter = NavLocalDateConverter::class)
    val friskmeldtDato: LocalDate?,

    @Column(name = "IS10_MAX", columnDefinition = "DECIMAL")
    @Convert(converter = NavLocalDateConverter::class)
    val maksdato: LocalDate?,

    @OneToMany
    @JoinColumns(value = [
        JoinColumn(name = "IS01_PERSONKEY", referencedColumnName = "IS01_PERSONKEY"),
        JoinColumn(name = "IS10_ARBUFOER_SEQ", referencedColumnName = "IS10_ARBUFOER_SEQ")
    ])
    @Cascade(value = [CascadeType.ALL])
    val utbetalingshistorikk: List<Utbetaling>,

    @OneToMany
    @JoinColumns(value = [
        JoinColumn(name = "IS01_PERSONKEY", referencedColumnName = "IS01_PERSONKEY"),
        JoinColumn(name = "IS10_ARBUFOER_SEQ", referencedColumnName = "IS10_ARBUFOER_SEQ")
    ])
    @Cascade(value = [CascadeType.ALL])
    val inntekter: List<Inntekt>
) : Serializable {
    val utbetalinger: List<Utbetaling>
        get() {
            // Fra https://confluence.adeo.no/display/INFOTRYGD/Tjeneste+finnGrunnlag+-+Informasjonsmodell
            // IS15-perioder som er tilbakeført eller korrigert skal ikke tas med i uttrekk, dvs. IS15-TYPE = '7' eller IS15-KORR not = space.
            return utbetalingshistorikk.filter { it.type != "7" && it.korr.isNullOrBlank() }
        }

    val tema: Tema
        get() {
            val tema: Tema? = stoenadstype?.tema

            if(tema != null) {
                return tema
            }

            return Tema.UKJENT
        }

    val opphoerFom: LocalDate?
        get() = stoppdato
            ?: friskmeldtDato
            ?: arbufoerTom?.plusDays(1)
            ?: maksdato

    fun innenforPeriode(fom: LocalDate, tom: LocalDate?): Boolean {
        if(tom != null) {
            require(fom == tom || fom.isBefore(tom)) { "Tom-dato kan ikke være før fom-dato." }
        }

        if(tom != null && tom.isBefore(arbufoer)) {
            return false
        }

        if(opphoerFom != null && fom.isAfter(opphoerFom)) {
            return false
        }

        return true
    }
}