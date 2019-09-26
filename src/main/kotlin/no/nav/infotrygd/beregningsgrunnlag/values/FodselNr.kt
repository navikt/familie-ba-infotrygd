package no.nav.infotrygd.beregningsgrunnlag.values

import com.fasterxml.jackson.annotation.JsonValue
import java.lang.IllegalArgumentException

data class FodselNr(@JsonValue val asString: String) {
    init {
        if(!"""\d{11}""".toRegex().matches(asString)) {
            throw IllegalArgumentException("Ikke et gyldig fødselsnummer: $asString")
        }
    }

    val kjoenn: Kjoenn
        get() {
            val kjoenn = asString.slice(8 until 9).toInt()
            return if(kjoenn % 2 == 0) Kjoenn.KVINNE else Kjoenn.MANN
        }
}