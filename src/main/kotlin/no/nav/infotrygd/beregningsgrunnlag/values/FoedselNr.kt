package no.nav.infotrygd.beregningsgrunnlag.values

import com.fasterxml.jackson.annotation.JsonValue
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

    fun finnSisteMuligeFoedselsdatoFoer(dato: LocalDate): LocalDate { // todo: FEIL, FEIL, FEIL!! Gå i register!
        val fnrYear = asString.slice(4 until 6).toInt()
        val fnrMonth = asString.slice(2 until 4).toInt()

        val dayFelt = asString.slice(0 until 2).toInt()
        val fnrDay = if(dNummer) dayFelt - 40 else dayFelt

        val hundre = (dato.year / 100) * 100 // Rund ned til nærmeste århundre

        val sammeAarhundre = LocalDate.of(hundre + fnrYear, fnrMonth, fnrDay)
        return if(sammeAarhundre.isBefore(dato)) {
            sammeAarhundre
        } else {
            LocalDate.of(hundre - 100 + fnrYear, fnrMonth, fnrDay)
        }
    }

    companion object {
        fun fraReversert(reversert: String): FoedselNr = FoedselNr(reverse(reversert))
    }
}

private val regex = """(\d\d)(\d\d)(\d\d)(\d{5})""".toRegex()

private fun reverse(fnr: String): String {
    require(regex.matches(fnr)) { "Ikke et gyldig (reversert?) fødselsnummer: $fnr" }

    val (a, b, c, pnr) = regex.find(fnr)!!.destructured
    return "$c$b$a$pnr"
}