package no.nav.familie.ba.infotrygd.model.db2

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.io.Serializable

@Embeddable
data class DelytelseId(
    @Column(name = "VEDTAK_ID", columnDefinition = "DECIMAL")
    val vedtakId: Long,
    @Column(name = "LINJE_ID", columnDefinition = "DECIMAL")
    val linjeId: Long?,
) : Serializable
