package no.nav.infotrygd.beregningsgrunnlag.model

import no.nav.infotrygd.beregningsgrunnlag.model.converters.NavLocalDateConverter
import no.nav.infotrygd.beregningsgrunnlag.model.converters.ReversedFodselNrConverter
import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Behandlingstema
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
    val behandlingstema: Behandlingstema,

    @Column(name = "F_NR", columnDefinition = "CHAR")
    @Convert(converter = ReversedFodselNrConverter::class)
    val fnr: FodselNr,

    @Column(name = "IS10_FRISK", columnDefinition = "CHAR")
    val frisk: String?,

    @Column(name = "IS10_ARBUFOER", columnDefinition = "DECIMAL")
    @Convert(converter = NavLocalDateConverter::class)
    val arbufoer: LocalDate,

    @Column(name = "IS10_STOPPDATO", columnDefinition = "DECIMAL")
    @Convert(converter = NavLocalDateConverter::class)
    val stoppdato: LocalDate?,

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumns(value = [
        JoinColumn(name = "IS01_PERSONKEY", referencedColumnName = "IS01_PERSONKEY"),
        JoinColumn(name = "IS10_ARBUFOER_SEQ", referencedColumnName = "IS10_ARBUFOER_SEQ")
    ])
    @Cascade(value = [CascadeType.ALL])
    val utbetalinger: List<Utbetaling>
) : Serializable