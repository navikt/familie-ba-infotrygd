package no.nav.familie.ba.infotrygd.model.dl1

import no.nav.familie.ba.infotrygd.model.converters.SakStatusConverter
import no.nav.familie.ba.infotrygd.model.converters.StatusLopenrConverter
import no.nav.familie.ba.infotrygd.model.kodeverk.SakStatus
import javax.persistence.*

@Entity
@Table(name = "SA_STATUS_15")
data class Status(
    @Id
    @Column(name = "ID_STATUS", nullable = false, columnDefinition = "DECIMAL")
    val id: Long,

    @Column(name = "REGION", columnDefinition = "CHAR")
    val region: String,

    @Column(name = "S01_PERSONKEY", columnDefinition = "DECIMAL")
    val personKey: Long,

    @Column(name = "S05_SAKSBLOKK", columnDefinition = "CHAR")
    val saksblokk: String,

    @Column(name = "S10_SAKSNR", columnDefinition = "CHAR")
    val saksnummer: String,

    @Column(name = "S15_LOPENR", columnDefinition = "CHAR")
    @Convert(converter = StatusLopenrConverter::class)
    val lopeNr: Long,

    @Column(name = "S15_STATUS", columnDefinition = "CHAR")
    @Convert(converter = SakStatusConverter::class)
    val status: SakStatus
)