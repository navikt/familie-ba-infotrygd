package no.nav.familie.ba.infotrygd.model.db2

import jakarta.persistence.*

@Entity
@Table(name = "T_BESLUT")
data class Beslutning(
    @Id
    @Column(name = "BESLUTNING_ID", columnDefinition = "DECIMAL")
    val beslutningId: Long,

    @Column(name = "VEDTAK_ID", columnDefinition = "DECIMAL")
    val vedtakId: Long,

    @Column(name = "GODKJENT2", columnDefinition = "CHAR(1 CHAR)")
    val godkjent2: String
)