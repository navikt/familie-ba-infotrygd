package no.nav.infotrygd.beregningsgrunnlag.model.converters

import no.nav.infotrygd.beregningsgrunnlag.values.FoedselNr
import javax.persistence.AttributeConverter

class ReversedFoedselNrConverter : AttributeConverter<FoedselNr?, String?> {
    override fun convertToDatabaseColumn(attribute: FoedselNr?): String? {
        return attribute?.reversert ?: "00000000000"
    }

    override fun convertToEntityAttribute(dbData: String?): FoedselNr? {
        if(dbData == null) {
            return null
        }

        if(dbData.toLong() == 0L) {
            return null
        }

        return FoedselNr.fraReversert(dbData)
    }
}