package no.nav.infotrygd.beregningsgrunnlag.model

import no.nav.infotrygd.beregningsgrunnlag.model.converters.*
import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Arbeidskategori
import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Frisk
import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Stoenadstype
import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Tema
import no.nav.infotrygd.beregningsgrunnlag.values.FoedselNr
import no.nav.infotrygd.beregningsgrunnlag.values.Kjoenn
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
    val fnr: FoedselNr,

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

    @Column(name = "IS10_ARBUFOER_OPPR", columnDefinition = "DECIMAL")
    @Convert(converter = NavLocalDateConverter::class)
    val arbufoerOpprinnelig: LocalDate,

    @Column(name = "IS10_DEKNINGSGRAD", columnDefinition = "DECIMAL")
    val dekningsgrad: Int?,

    @Column(name = "IS10_FDATO", columnDefinition = "DECIMAL")
    @Convert(converter = NavLocalDateConverter::class)
    val foedselsdatoBarn: LocalDate?,

    @Column(name = "IS10_TIDSK_BARNFNR", columnDefinition = "CHAR")
    @Convert(converter = ReversedFoedselNrConverter::class)
    val barnFnr: FoedselNr?,

    @Column(name = "IS10_STEBARNSADOPSJON", columnDefinition = "CHAR")
    val stebarnsadopsjon: String?,

    @Column(name = "IS10_ARBKAT", columnDefinition = "CHAR")
    val arbeidskategori: Arbeidskategori?,

    @Column(name = "IS10_REG_DATO", columnDefinition = "CHAR")
    @Convert(converter = NavCharDateConverter::class)
    val regdato: LocalDate?,

    @Column(name = "IS10_BRUKERID", columnDefinition = "CHAR")
    @Convert(converter = BrukerIdConverter::class)
    val brukerId: String?,

    @Column(name = "IS10_PROS_INNTEKT_GR", columnDefinition = "CHAR")
    @Convert(converter = UtbetalingsgradConverter::class)
    val inntektsgrunnlagProsent: Int?,

    @Column(name = "IS10_STOPPDATO", columnDefinition = "DECIMAL")
    @Convert(converter = NavLocalDateConverter::class)
    val stoppdato: LocalDate?,

    @Column(name = "TK_NR", columnDefinition = "CHAR")
    val tkNr: String,

    @OneToMany
    @JoinColumns(value = [
        JoinColumn(name = "IS01_PERSONKEY", referencedColumnName = "IS01_PERSONKEY"),
        JoinColumn(name = "IS10_ARBUFOER_SEQ", referencedColumnName = "IS10_ARBUFOER_SEQ")
    ])
    @Cascade(value = [CascadeType.ALL])
    val utbetalinger: List<Utbetaling>,

    @OneToMany
    @JoinColumns(value = [
        JoinColumn(name = "IS01_PERSONKEY", referencedColumnName = "IS01_PERSONKEY"),
        JoinColumn(name = "IS10_ARBUFOER_SEQ", referencedColumnName = "IS10_ARBUFOER_SEQ")
    ])
    @Cascade(value = [CascadeType.ALL])
    val inntekter: List<Inntekt>
) : Serializable {
    val tema: Tema
        get() {
            val tema: Tema? = stoenadstype?.tema

            if(tema != null) {
                return tema
            }

            return Tema.UKJENT
        }

    val barnPersonKey: Long?
        get() {
            val fnr = barnFnr ?: return null
            return "$tkNr${fnr.reversert}".toLong()
        }

    val barnKode: String
        get() {
            val sammeKjoennMor = stebarnsadopsjon in setOf("A", "D")
            if (sammeKjoennMor) {
                return "1"
            }

            val sammeKjoennFar = stebarnsadopsjon in setOf("B", "C", "E")
            if (sammeKjoennFar) {
                return "2"
            }

            return when (fnr.kjoenn) {
                Kjoenn.KVINNE -> "1"
                Kjoenn.MANN -> "2"
            }
        }
}