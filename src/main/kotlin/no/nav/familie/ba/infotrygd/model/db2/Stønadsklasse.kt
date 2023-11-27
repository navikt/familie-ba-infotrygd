package no.nav.familie.ba.infotrygd.model.db2

import java.io.Serializable
import jakarta.persistence.*

@Entity
@Table(name = "T_STONADSKLASSE")
@IdClass(StønadsklasseId::class)
data class Stønadsklasse(
    @Id
    @Column(name = "VEDTAK_ID", columnDefinition = "DECIMAL")
    val vedtakId: Long,

    @Id
    @Column(name = "KODE_NIVAA", columnDefinition = "VARCHAR2")
    val kodeNivå: String,

    @Id
    @Column(name = "KODE_KLASSE", columnDefinition = "VARCHAR2")
    val kodeKlasse: String
)

class StønadsklasseId(
    val vedtakId: Long? = null,
    val kodeNivå: String? = null,
    val kodeKlasse: String? = null
) :Serializable
