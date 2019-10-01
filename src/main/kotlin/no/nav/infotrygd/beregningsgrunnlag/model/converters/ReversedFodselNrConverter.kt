package no.nav.infotrygd.beregningsgrunnlag.model.converters

import no.nav.infotrygd.beregningsgrunnlag.values.FoedselNr
import javax.persistence.AttributeConverter

class ReversedFodselNrConverter : AttributeConverter<FoedselNr?, String?> {
    override fun convertToDatabaseColumn(attribute: FoedselNr?): String? {
        return attribute?.let { attribute.reversert }
    }

    override fun convertToEntityAttribute(dbData: String?): FoedselNr? {
        return dbData?.let { FoedselNr.fraReversert(dbData) }
    }
}