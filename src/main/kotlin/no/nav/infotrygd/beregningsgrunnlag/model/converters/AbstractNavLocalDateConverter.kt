package no.nav.infotrygd.beregningsgrunnlag.model.converters

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.persistence.AttributeConverter

open class AbstractNavLocalDateConverter(datePattern: String) : AttributeConverter<LocalDate?, Int?> {
    private val formatter = DateTimeFormatter.ofPattern(datePattern);
    override fun convertToDatabaseColumn(attribute: LocalDate?): Int? {
        return attribute?.format(formatter)?.toInt()
    }

    override fun convertToEntityAttribute(dbData: Int?): LocalDate? {
        if(dbData == null || dbData == NULL_VALUE) {
            return null
        }
        return LocalDate.from(formatter.parse(String.format("%08d", dbData)))
    }

    companion object {
        const val NULL_VALUE = 0
    }
}
