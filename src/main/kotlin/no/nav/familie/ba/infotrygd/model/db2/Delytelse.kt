package no.nav.familie.ba.infotrygd.model.db2


import no.nav.familie.ba.infotrygd.model.converters.Char2Converter
import no.nav.familie.ba.infotrygd.model.converters.CharConverter
import java.time.LocalDate
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table
import no.nav.familie.kontrakter.ba.infotrygd.Delytelse as DelytelseDto


@Entity
@Table(name = "T_DELYTELSE")
data class Delytelse(

    @EmbeddedId
    val id: DelytelseId,

    @Column(name = "FOM", columnDefinition = "DATE")
    val fom: LocalDate,

    @Column(name = "TOM", columnDefinition = "DATE")
    val tom: LocalDate? = null,

    @Column(name = "BELOP", columnDefinition = "DECIMAL")
    val beløp: Double,

    @Column(name = "TYPE_DELYTELSE", columnDefinition = "CHAR")
    @Convert(converter = Char2Converter::class)
    val typeDelytelse: String,

    @Column(name = "OPPGJORSORDNING", columnDefinition = "CHAR")
    @Convert(converter = CharConverter::class)
    val oppgjørsordning: String? = null,

    @Column(name = "TYPE_UTBETALING", columnDefinition = "CHAR")
    @Convert(converter = CharConverter::class)
    val typeUtbetaling: String,
)

fun Delytelse.toDelytelseDto(): DelytelseDto {
    return DelytelseDto(this.fom, this.tom, this.beløp, this.typeDelytelse, this.oppgjørsordning, this.typeUtbetaling)
}