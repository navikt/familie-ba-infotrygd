package no.nav.familie.ba.infotrygd.model.converters

import no.nav.commons.foedselsnummer.FoedselsNr
import jakarta.persistence.AttributeConverter

class FoedselNrConverter  : AttributeConverter<FoedselsNr?, String?> {
    override fun convertToDatabaseColumn(attribute: FoedselsNr?): String? {
        return attribute?.asString
    }

    override fun convertToEntityAttribute(dbData: String?): FoedselsNr? {
        return dbData?.let { FoedselsNr(it) }
    }
}