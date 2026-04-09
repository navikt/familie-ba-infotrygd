package no.nav.familie.ba.infotrygd.model.db2

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "T_ENDRING")
data class Endring(
    @Id
    @Column(name = "VEDTAK_ID", columnDefinition = "DECIMAL")
    val vedtakId: Long,
    @Column(name = "KODE", columnDefinition = "VARCHAR2")
    val kode: String,
)
