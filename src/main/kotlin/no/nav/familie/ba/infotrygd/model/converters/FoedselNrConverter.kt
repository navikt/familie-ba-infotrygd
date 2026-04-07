package no.nav.familie.ba.infotrygd.model.converters

import jakarta.persistence.AttributeConverter
import no.nav.commons.foedselsnummer.FoedselsNr

class FoedselNrConverter : AttributeConverter<FoedselsNr?, String?> {
    override fun convertToDatabaseColumn(attribute: FoedselsNr?): String? = attribute?.asString

    override fun convertToEntityAttribute(dbData: String?): FoedselsNr? = dbData?.let { FoedselsNr(it) }
}
