package no.nav.familie.ba.infotrygd.model.converters

import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import jakarta.persistence.AttributeConverter

open class AbstractNavLocalDateConverter(datePattern: String) : AttributeConverter<LocalDate?, Int?> {
    private val logger = LoggerFactory.getLogger(javaClass)
    
    private val formatter = DateTimeFormatter.ofPattern(datePattern)
    override fun convertToDatabaseColumn(attribute: LocalDate?): Int? {
        return attribute?.format(formatter)?.toInt()
    }

    override fun convertToEntityAttribute(dbData: Int?): LocalDate? {
        if(dbData == null || dbData == NULL_VALUE) {
            return null
        }
        return try {
            LocalDate.from(formatter.parse(String.format("%08d", dbData)))
        } catch (e: Exception) {
            /*
                Det finnes datoer i databasen som er ugyldige fordi at de er ført feil i kombinasjon
                med manglende input-validering. I disse tilfellene så har vi ikke informasjon om dato
                så vi returnerer et tomt resultat.
             */
            logger.warn("Kunne ikke lese dato: '$dbData'. \nFiltrert stacktrace:" +
                    "${e.stackTrace.filter { it.className.contains("no.nav.familie.ba.infotrygd") }.toList()} ")
            null
        }
    }

    companion object {
        const val NULL_VALUE = 0
    }
}
