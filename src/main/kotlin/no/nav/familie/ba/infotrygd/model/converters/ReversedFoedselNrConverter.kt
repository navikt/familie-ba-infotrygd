package no.nav.familie.ba.infotrygd.model.converters

import jakarta.persistence.AttributeConverter
import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.familie.ba.infotrygd.utils.fraReversert
import no.nav.familie.ba.infotrygd.utils.reversert

class ReversedFoedselNrConverter : AttributeConverter<FoedselsNr?, String?> {
    override fun convertToDatabaseColumn(attribute: FoedselsNr?): String? = attribute?.reversert ?: "00000000000"

    override fun convertToEntityAttribute(dbData: String?): FoedselsNr? {
        if (dbData == null) {
            return null
        }

        if (dbData.toLong() == 0L) {
            return null
        }

        return FoedselsNr.fraReversert(dbData)
    }
}
