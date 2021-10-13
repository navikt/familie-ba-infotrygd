package no.nav.familie.ba.infotrygd.model.converters

import javax.persistence.AttributeConverter
import javax.persistence.Converter

abstract class AbstractCharConverter(private val size: Int) : AttributeConverter<String?, String?> {
    override fun convertToDatabaseColumn(attribute: String?): String? {
        val str = attribute ?: ""
        return str.padEnd(size, ' ')
    }

    override fun convertToEntityAttribute(dbData: String?): String? {
        if(dbData.isNullOrBlank()) {
            return null
        }

        return dbData.trimEnd()
    }

}

@Converter
class BrukerIdConverter : AbstractCharConverter(7)

@Converter
class Char3Converter: AbstractCharConverter(3)

@Converter
class Char2Converter : AbstractCharConverter(2)

@Converter
class CharConverter :AbstractCharConverter(1)