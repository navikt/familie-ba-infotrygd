package no.nav.infotrygd.barnetrygd.model.converters

import javax.persistence.AttributeConverter

class StatusLopenrConverter : AttributeConverter<Long?, String?> {
    override fun convertToDatabaseColumn(attribute: Long?): String? {
        return attribute?.let { String.format("%02d", it) }
    }

    override fun convertToEntityAttribute(dbData: String?): Long? {
        return dbData?.toLong()
    }
}