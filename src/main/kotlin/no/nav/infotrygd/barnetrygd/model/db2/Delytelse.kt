package no.nav.infotrygd.barnetrygd.model.db2


import no.nav.infotrygd.barnetrygd.model.converters.Char2Converter
import no.nav.infotrygd.barnetrygd.model.converters.CharConverter
import no.nav.familie.kontrakter.ba.infotrygd.Delytelse as DelytelseDto
import java.time.LocalDate
import javax.persistence.*


@Entity
@Table(name = "T_DELYTELSE")
data class Delytelse(
    @Id
    @Column(name = "VEDTAK_ID", columnDefinition = "DECIMAL")
    val vedtakId: Long,

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