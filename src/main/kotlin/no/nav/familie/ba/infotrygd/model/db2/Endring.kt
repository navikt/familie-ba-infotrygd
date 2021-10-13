package no.nav.familie.ba.infotrygd.model.db2

import javax.persistence.*

@Entity
@Table(name = "T_ENDRING")
data class Endring(
    @Id
    @Column(name = "VEDTAK_ID", columnDefinition = "DECIMAL")
    val vedtakId: Long,

    @Column(name = "KODE", columnDefinition = "VARCHAR2")
    val kode: String,
)