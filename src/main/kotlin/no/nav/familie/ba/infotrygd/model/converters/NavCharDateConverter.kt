package no.nav.familie.ba.infotrygd.model.converters

import java.time.LocalDate
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class NavCharDateConverter : AttributeConverter<LocalDate?, String?> {
    private val converter = NavLocalDateConverter()

    override fun convertToDatabaseColumn(attribute: LocalDate?): String? {
        return converter.convertToDatabaseColumn(attribute)?.toString()
    }

    override fun convertToEntityAttribute(dbData: String?): LocalDate? {
        return dbData?.let { converter.convertToEntityAttribute(it.toInt()) }
    }
}