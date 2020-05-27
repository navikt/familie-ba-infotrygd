package no.nav.infotrygd.barnetrygd.model.converters

import no.nav.infotrygd.barnetrygd.model.kodeverk.Frisk
import java.lang.IllegalStateException
import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter
class FriskConverter : AttributeConverter<Frisk, String> {
    override fun convertToDatabaseColumn(attribute: Frisk): String {
        return attribute.kode
    }

    override fun convertToEntityAttribute(dbData: String): Frisk {
        return Frisk.values().find { it.kode.trimEnd() == dbData.trimEnd() } ?: throw IllegalStateException("Ukjent databaseverdi: '$dbData'")
    }
}