package no.nav.familie.ba.infotrygd.model.db2

import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
data class DelytelseId(
    @Column(name = "VEDTAK_ID", columnDefinition = "DECIMAL")
    val vedtakId: Long,
    @Column(name = "LINJE_ID", columnDefinition = "DECIMAL")
    val linjeId: Long
) : Serializable