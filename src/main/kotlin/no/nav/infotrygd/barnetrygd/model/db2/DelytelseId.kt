package no.nav.infotrygd.barnetrygd.model.db2

import java.io.Serializable
import java.time.LocalDateTime
import javax.persistence.Embeddable

@Embeddable
data class DelytelseId(
    val vedtakId: Long,
    val type: String,
    val tidspunktRegistrert: LocalDateTime
) : Serializable