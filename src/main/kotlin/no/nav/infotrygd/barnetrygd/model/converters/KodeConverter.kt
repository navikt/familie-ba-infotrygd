package no.nav.infotrygd.barnetrygd.model.converters

import no.nav.infotrygd.barnetrygd.exception.UkjentDatabaseverdiException
import no.nav.infotrygd.barnetrygd.model.kodeverk.Kode
import no.nav.infotrygd.barnetrygd.model.kodeverk.SakStatus
import javax.persistence.AttributeConverter
import javax.persistence.Converter

abstract class KodeConverter<T : Kode>(private val koder: List<T>, val fieldSize: Int = 0, val padChar: Char = ' ') : AttributeConverter<T?, String?> {

    private val blankValue: T? = koder.find { it.kode.isBlank() }

    override fun convertToDatabaseColumn(attribute: T?): String? {
        if(attribute != null && attribute == blankValue) {
            return "".padEnd(length = fieldSize)
        }

        return attribute?.kode?.padEnd(length = fieldSize, padChar = padChar)
    }

    override fun convertToEntityAttribute(dbData: String?): T? {
        if(dbData == null) {
            return null
        }

        if(dbData.isBlank()) {
            return blankValue
        }

        val normalisertKode = dbData.trim()
        for(verdi in koder) {
            if (verdi.kode == normalisertKode) {
                return verdi
            }
        }

        throw UkjentDatabaseverdiException(normalisertKode, koder.map { it.kode })
    }
}

@Converter(autoApply = true)
class SakStatusConverter : KodeConverter<SakStatus>(SakStatus.values().toList(), fieldSize = 2)
