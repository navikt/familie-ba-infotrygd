package no.nav.infotrygd.beregningsgrunnlag.model.db2

import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Stoenadstype
import java.io.Serializable
import java.time.LocalDateTime
import javax.persistence.Embeddable

@Embeddable
data class DelytelseId(
    val vedtakId: Long,
    val type: Stoenadstype,
    val tidspunktRegistrert: LocalDateTime
) : Serializable