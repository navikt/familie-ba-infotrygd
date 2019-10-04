package no.nav.infotrygd.beregningsgrunnlag.model.db2

import java.io.Serializable
import java.time.LocalDateTime
import javax.persistence.Embeddable

@Embeddable
data class DelytelseId(
    val vedtakId: Long,
    val type: String,
    val tidspunktRegistrert: LocalDateTime
) : Serializable