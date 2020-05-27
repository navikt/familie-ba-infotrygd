package no.nav.infotrygd.barnetrygd.model.converters

import javax.persistence.AttributeConverter

class UtbetalingsgradConverter : AttributeConverter<Int?, String?> {

    private val size = 3 // Kolonnen er av type CHAR(3)

    override fun convertToDatabaseColumn(attribute: Int?): String? {
        if(attribute == null) {
            return "".padStart(size, padChar = ' ')
        }
        return attribute.toString().padStart(size, '0')
    }

    override fun convertToEntityAttribute(dbData: String?): Int? {
        if(dbData.isNullOrBlank()) {
            return null
        }

        val res = dbData.toInt()
        return if(res == 0) null else res
    }
}