package no.nav.familie.ba.infotrygd.model.db2

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import no.nav.familie.ba.infotrygd.model.converters.Char2Converter

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
