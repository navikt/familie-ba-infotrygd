package no.nav.infotrygd.beregningsgrunnlag.model.converters

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.infotrygd.beregningsgrunnlag.utils.fraReversert
import no.nav.infotrygd.beregningsgrunnlag.utils.reversert
import javax.persistence.AttributeConverter

class ReversedFoedselNrConverter : AttributeConverter<FoedselsNr?, String?> {
    override fun convertToDatabaseColumn(attribute: FoedselsNr?): String? {
        return attribute?.reversert ?: "00000000000"
    }

    override fun convertToEntityAttribute(dbData: String?): FoedselsNr? {
        if(dbData == null) {
            return null
        }

        if(dbData.toLong() == 0L) {
            return null
        }

        return FoedselsNr.fraReversert(dbData)
    }
}