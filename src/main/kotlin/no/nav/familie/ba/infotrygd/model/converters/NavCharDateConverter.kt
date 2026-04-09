package no.nav.familie.ba.infotrygd.model.converters

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import java.time.LocalDate

@Converter
class NavCharDateConverter : AttributeConverter<LocalDate?, String?> {
    private val converter = NavLocalDateConverter()

    override fun convertToDatabaseColumn(attribute: LocalDate?): String? = converter.convertToDatabaseColumn(attribute)?.toString()

    override fun convertToEntityAttribute(dbData: String?): LocalDate? = dbData?.let { converter.convertToEntityAttribute(it.toInt()) }
}
