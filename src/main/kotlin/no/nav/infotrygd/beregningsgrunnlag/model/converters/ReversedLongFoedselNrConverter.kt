package no.nav.infotrygd.beregningsgrunnlag.model.converters

import no.nav.infotrygd.beregningsgrunnlag.values.FoedselNr
import javax.persistence.AttributeConverter

class ReversedLongFoedselNrConverter : AttributeConverter<FoedselNr?, Long?> {
    private val converter = ReversedFoedselNrConverter()

    override fun convertToDatabaseColumn(attribute: FoedselNr?): Long? {
        return converter.convertToDatabaseColumn(attribute)?.toLong() ?: 0
    }

    override fun convertToEntityAttribute(dbData: Long?): FoedselNr? {
        return converter.convertToEntityAttribute(dbData?.toString()?.padStart(11, '0'))
    }
}