package no.nav.infotrygd.beregningsgrunnlag.model.converters

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

internal class RefusjonJaNeiConverterTest {

    private val converter = RefusjonJaNeiConverter()

    @Test
    fun convertToDatabaseColumn() {
        assertThat(converter.convertToDatabaseColumn(true)).isEqualTo("J")
        assertThat(converter.convertToDatabaseColumn(false)).isEqualTo(" ")
        assertThat(converter.convertToDatabaseColumn(null)).isEqualTo(" ")
    }

    @Test
    fun convertToEntityAttribute() {
        assertThat(converter.convertToEntityAttribute("J")).isEqualTo(true)
        assertThat(converter.convertToEntityAttribute("D")).isEqualTo(true)
        assertThat(converter.convertToEntityAttribute("S")).isEqualTo(true)
        assertThat(converter.convertToEntityAttribute("H")).isEqualTo(true)
        assertThat(converter.convertToEntityAttribute(" ")).isEqualTo(false)
        assertThat(converter.convertToEntityAttribute(null)).isEqualTo(false)
    }
}