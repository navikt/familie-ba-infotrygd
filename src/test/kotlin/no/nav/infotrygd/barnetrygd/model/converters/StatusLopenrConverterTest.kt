package no.nav.infotrygd.barnetrygd.model.converters

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class StatusLopenrConverterTest {
    val converter: StatusLopenrConverter =
        StatusLopenrConverter()

    @Test
    fun convertToDatabaseColumn() {
        val result = converter.convertToDatabaseColumn(2)
        assertThat(result).isEqualTo("02")

        assertThat(converter.convertToDatabaseColumn(null)).isNull()
    }

    @Test
    fun convertToEntityAttribute() {
        val result = converter.convertToEntityAttribute("02")
        assertThat(result).isEqualTo(2)

        assertThat(converter.convertToEntityAttribute(null)).isNull()
    }
}