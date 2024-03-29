package no.nav.familie.ba.infotrygd.model.converters

import no.nav.commons.foedselsnummer.FoedselsNr
import jakarta.persistence.AttributeConverter

class ReversedLongFoedselNrConverter : AttributeConverter<FoedselsNr?, Long?> {
    private val converter = ReversedFoedselNrConverter()

    override fun convertToDatabaseColumn(attribute: FoedselsNr?): Long? {
        return converter.convertToDatabaseColumn(attribute)?.toLong() ?: 0
    }

    override fun convertToEntityAttribute(dbData: Long?): FoedselsNr? {
        return converter.convertToEntityAttribute(dbData?.toString()?.padStart(11, '0'))
    }
}