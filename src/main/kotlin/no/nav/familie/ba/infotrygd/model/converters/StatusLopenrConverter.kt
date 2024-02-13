package no.nav.familie.ba.infotrygd.model.converters

import jakarta.persistence.AttributeConverter

class StatusLopenrConverter : AttributeConverter<Long?, String?> {
    override fun convertToDatabaseColumn(attribute: Long?): String? {
        return attribute?.let { String.format("%02d", it) }
    }

    override fun convertToEntityAttribute(dbData: String?): Long? {
        return dbData?.toLong()
    }
}