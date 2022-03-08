package no.nav.familie.ba.infotrygd.utils

import java.time.YearMonth
import java.time.format.DateTimeFormatter

object DatoUtils {
    fun seqDatoTilYearMonth(seqDato: String): YearMonth? {
        if (seqDato == "000000") return null
        val intDato = 999999 - seqDato.toInt()
        return YearMonth.parse("$intDato", DateTimeFormatter.ofPattern("yyyyMM"))
    }

    fun stringDatoMMyyyyTilYearMonth(stringDato: String): YearMonth? {
        if (stringDato == "000000") return null
        return YearMonth.parse(stringDato, DateTimeFormatter.ofPattern("MMyyyy"))
    }

    fun YearMonth.isSameOrAfter(toCompare: YearMonth): Boolean {
        return this.isAfter(toCompare) || this == toCompare
    }
}