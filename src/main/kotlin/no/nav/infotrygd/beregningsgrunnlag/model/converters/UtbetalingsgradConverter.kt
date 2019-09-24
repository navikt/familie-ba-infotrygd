package no.nav.infotrygd.beregningsgrunnlag.model.converters

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

        return dbData.toInt()
    }
}