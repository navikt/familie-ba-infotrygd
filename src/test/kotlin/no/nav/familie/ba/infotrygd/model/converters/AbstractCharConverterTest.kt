package no.nav.familie.ba.infotrygd.model.converters

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class AbstractCharConverterTest {

    private object converter : AbstractCharConverter(5)

    @Test
    fun convertToDatabaseColumn() {
        assertThat(converter.convertToDatabaseColumn("x")).isEqualTo("x    ")
        assertThat(converter.convertToDatabaseColumn(null)).isEqualTo("     ")
    }

    @Test
    fun convertToEntityAttribute() {
        assertThat(converter.convertToEntityAttribute("     ")).isNull()
        assertThat(converter.convertToEntityAttribute(null)).isNull()
        assertThat(converter.convertToEntityAttribute("x    ")).isEqualTo("x")
    }
}