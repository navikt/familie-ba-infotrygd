package no.nav.infotrygd.beregningsgrunnlag.model

import no.nav.infotrygd.beregningsgrunnlag.model.converters.NavLocalDateConverter
import no.nav.infotrygd.beregningsgrunnlag.model.converters.ReversedFodselNrConverter
import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Arbeidskategori
import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Stoenadstype
import no.nav.infotrygd.beregningsgrunnlag.values.FodselNr
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
    @Convert(converter = ReversedFodselNrConverter::class)
    val fnr: FodselNr,

    @Column(name = "IS10_FRISK", columnDefinition = "CHAR")
    val frisk: String?,

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

    @Column(name = "IS10_ARBKAT_99", columnDefinition = "CHAR")
    val arbeidskategori: Arbeidskategori?,

    // todo: gradering

    @Column(name = "IS10_STOPPDATO", columnDefinition = "DECIMAL")
    @Convert(converter = NavLocalDateConverter::class)
    val stoppdato: LocalDate?,

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
    val ytelse: Ytelse
        get() {
            val ytelse: Ytelse? = when(stoenadstype) {
                Stoenadstype.SYKEPENGER
                    -> Ytelse.SYKEPENGER

                Stoenadstype.FOEDSEL,
                Stoenadstype.ADOPSJON,
                Stoenadstype.RISIKOFYLT_ARBMILJOE,
                Stoenadstype.SVANGERSKAP
                    -> Ytelse.FORELDREPENGER

                Stoenadstype.BARNS_SYKDOM,
                Stoenadstype.ALV_SYKT_BARN,
                Stoenadstype.KURS_KAP_3_23,
                Stoenadstype.PAS_DOEDSSYK,
                Stoenadstype.PLEIEPENGER_INSTOPPH,
                Stoenadstype.PLEIEPENGER_NY_ORDNING
                    -> Ytelse.PAAROERENDE_SYKDOM

                else -> null
            }

            if(ytelse != null) {
                return ytelse
            }

            return Ytelse.UKJENT
        }
}