package no.nav.infotrygd.beregningsgrunnlag.model.db2

import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Inntektsperiode
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import javax.persistence.*

@Entity
@Table(name = "T_INNTEKT")
@IdClass(InntektId::class)
data class Inntekt(
    @Id
    @Column(name = "STONAD_ID", nullable = false, columnDefinition = "DECIMAL")
    val stonadId: Long,

    @Id
    @Column(name = "ORGNR", nullable = false, columnDefinition = "DECIMAL")
    val orgNr: Long,

    @Id
    @Column(name = "INNTEKT_FOM", nullable = false, columnDefinition = "DATE")
    val inntektFom: LocalDate,

    @Id
    @Column(name = "LOPENR", nullable = false, columnDefinition = "DECIMAL")
    val lopeNr: Long,

    @Column(name = "STATUS", columnDefinition = "CHAR")
    val status: String,

    @Column(name = "INNTEKT", columnDefinition = "DECIMAL")
    val inntekt: BigDecimal,

    @Column(name = "PERIODE", columnDefinition = "CHAR")
    val periode: Inntektsperiode
)

@Embeddable
private data class InntektId(
    val stonadId: Long,
    val orgNr: Long,
    val inntektFom: LocalDate,
    val lopeNr: Long
) : Serializable