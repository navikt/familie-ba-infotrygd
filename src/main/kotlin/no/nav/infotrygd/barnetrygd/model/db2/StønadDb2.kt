package no.nav.infotrygd.barnetrygd.model.db2

import no.nav.infotrygd.barnetrygd.model.converters.Char2Converter
import javax.persistence.*

@Entity
@Table(name = "T_STONAD")
data class StønadDb2(
    @Id
    @Column(name = "STONAD_ID", columnDefinition = "DECIMAL")
    val stønadId: Long,

    @Column(name = "KODE_RUTINE", columnDefinition = "CHAR")
    @Convert(converter = Char2Converter::class)
    val kodeRutine: String,

    @Column(name = "PERSON_LOPENR", columnDefinition = "DECIMAL")
    val løpenummer: Long,
)