package no.nav.infotrygd.beregningsgrunnlag.values

import com.fasterxml.jackson.annotation.JsonValue
import java.lang.IllegalStateException
import java.time.LocalDate

data class FoedselNr(@JsonValue val asString: String) {
    init {
        require("""\d{11}""".toRegex().matches(asString)) { "Ikke et gyldig fødselsnummer: $asString" }
    }

    val kjoenn: Kjoenn
        get() {
            val kjoenn = asString.slice(8 until 9).toInt()
            return if(kjoenn % 2 == 0) Kjoenn.KVINNE else Kjoenn.MANN
        }

    val reversert: String
        get() = reverse(asString)

    val dNummer: Boolean
        get() {
            return asString[0].toString().toInt() >= 4
        }

    val foedselsdato: LocalDate
        get() {
            val fnrMonth = asString.slice(2 until 4).toInt()

            val dayFelt = asString.slice(0 until 2).toInt()
            val fnrDay = if(dNummer) dayFelt - 40 else dayFelt

            // todo: interne fnr. (H/FH)

            return LocalDate.of(foedselsaar, fnrMonth, fnrDay)
        }

    private val individnummer: Int
        get() {
            return asString.slice(6 until 9).toInt()
        }

    private val foedselsaar: Int
        get() {
            val fnrYear = asString.slice(4 until 6)

            for((individSerie, aarSerie) in serier) {
                val kandidat = (aarSerie.start.toString().slice(0 until 2) + fnrYear).toInt()
                if(individSerie.contains(individnummer) && aarSerie.contains(kandidat)) {
                    return kandidat
                }
            }
            throw IllegalStateException("Ugyldig individnummer: $individnummer")
        }

    companion object {
        fun fraReversert(reversert: String): FoedselNr = FoedselNr(reverse(reversert))

        val serier: List<Pair<ClosedRange<Int>, ClosedRange<Int>>>
            get() {
                return listOf(
                    500..749 to 1854..1899,
                      0..499 to 1900..1999,
                    900..999 to 1940..1999,
                    500..999 to 2000..2039
                )
            }
    }
}

private val regex = """(\d\d)(\d\d)(\d\d)(\d{5})""".toRegex()

private fun reverse(fnr: String): String {
    require(regex.matches(fnr)) { "Ikke et gyldig (reversert?) fødselsnummer: $fnr" }

    val (a, b, c, pnr) = regex.find(fnr)!!.destructured
    return "$c$b$a$pnr"
}