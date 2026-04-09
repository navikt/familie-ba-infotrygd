package no.nav.familie.ba.infotrygd.model.converters

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AbstractCharConverterTest {
    private object Converter : AbstractCharConverter(5)

    @Test
    fun convertToDatabaseColumn() {
        assertThat(Converter.convertToDatabaseColumn("x")).isEqualTo("x    ")
        assertThat(Converter.convertToDatabaseColumn(null)).isEqualTo("     ")
    }

    @Test
    fun convertToEntityAttribute() {
        assertThat(Converter.convertToEntityAttribute("     ")).isNull()
        assertThat(Converter.convertToEntityAttribute(null)).isNull()
        assertThat(Converter.convertToEntityAttribute("x    ")).isEqualTo("x")
    }
}
