package no.nav.infotrygd.beregningsgrunnlag.model.converters

import no.nav.infotrygd.beregningsgrunnlag.values.FodselNr
import java.lang.IllegalArgumentException
import javax.persistence.AttributeConverter

class ReversedFodselNrConverter : AttributeConverter<FodselNr?, String?> {
    private val regex = """(\d\d)(\d\d)(\d\d)(\d{5})""".toRegex()

    private fun reverse(fnr: String): String {
        if(!regex.matches(fnr)) {
            throw IllegalArgumentException("Ikke et gyldig (reversert?) fødselsnummer: $fnr")
        }

        val (a, b, c, pnr) = regex.find(fnr)!!.destructured
        return "$c$b$a$pnr"
    }

    override fun convertToDatabaseColumn(attribute: FodselNr?): String? {
        return attribute?.let { reverse(it.asString) }
    }

    override fun convertToEntityAttribute(dbData: String?): FodselNr? {
        return dbData?.let { FodselNr(reverse(it)) }
    }
}