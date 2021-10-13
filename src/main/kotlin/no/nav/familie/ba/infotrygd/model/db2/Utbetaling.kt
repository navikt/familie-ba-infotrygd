package no.nav.familie.ba.infotrygd.model.db2


import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.familie.ba.infotrygd.model.converters.CharConverter
import no.nav.familie.ba.infotrygd.model.converters.ReversedFoedselNrConverter
import no.nav.familie.ba.infotrygd.utils.DatoUtils.seqDatoTilYearMonth
import no.nav.familie.ba.infotrygd.utils.DatoUtils.stringDatoMMyyyyTilYearMonth
import java.time.YearMonth
import javax.persistence.*


@Entity
@Table(name = "BA_UTBETALING_30")
data class Utbetaling(
    @Id
    @Column(name = "ID_BA_UTBET", columnDefinition = "DECIMAL")
    val utbetalingId: Long,

    @Column(name = "B01_PERSONKEY", columnDefinition = "DECIMAL")
    val personKey: Long,

    @Column(name = "F_NR", columnDefinition = "VARCHAR2")
    @Convert(converter = ReversedFoedselNrConverter::class)
    val fnr: FoedselsNr,

    @Column(name = "B30_START_UTBET_MND_SEQ", columnDefinition = "VARCHAR2")
    val startUtbetalingMåned: String,

    @Column(name = "B30_UTBET_TOM", columnDefinition = "VARCHAR2")
    val utbetalingTom: String,

    @Column(name = "B30_VFOM_SEQ", columnDefinition = "VARCHAR2")
    val virksomFom: String,

    @Column(name = "B30_BELOP", columnDefinition = "DECIMAL")
    val beløp: Double,

    @Column(name = "B30_KONTONR", columnDefinition = "VARCHAR2")
    val kontonummer: String,

    @Column(name = "B30_UTBET_TYPE", columnDefinition = "CHAR")
    @Convert(converter = CharConverter::class)
    val utbetalingstype: String,

) {
    fun fom(): YearMonth? {
        return seqDatoTilYearMonth(virksomFom)
    }

    fun tom(): YearMonth? {
        return stringDatoMMyyyyTilYearMonth(utbetalingTom)
    }

    fun erSmåbarnstillegg(): Boolean {
        return this.kontonummer == "06040000"
    }
}