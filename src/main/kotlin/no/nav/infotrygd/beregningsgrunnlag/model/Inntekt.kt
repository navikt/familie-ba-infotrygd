package no.nav.infotrygd.beregningsgrunnlag.model

import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Inntektsperiode
import java.math.BigDecimal
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "IS_INNTEKT_13")
data class Inntekt(
    @Id
    @Column(name = "ID_INNT", columnDefinition = "DECIMAL")
    val id: Long,

    @Column(name = "IS01_PERSONKEY", columnDefinition = "DECIMAL")
    val personKey: Long,

    @Column(name = "IS10_ARBUFOER_SEQ", columnDefinition = "DECIMAL")
    val arbufoerSeq: Long,

    @Column(name = "IS13_ARBGIVNR", columnDefinition = "DECIMAL")
    val arbgiverNr: String,

    @Column(name = "IS13_LOENN", columnDefinition = "DECIMAL")
    val loenn: BigDecimal,

    @Column(name = "IS13_PERIODE", columnDefinition = "CHAR")
    val periode: Inntektsperiode
)