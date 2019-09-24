package no.nav.infotrygd.beregningsgrunnlag.model.converters

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class UtbetalingsgradConverterTest {

    private val converter = UtbetalingsgradConverter()

    @Test
    fun convertToDatabaseColumn() {
        convertToDatabaseColumn(null, "   ")
        convertToDatabaseColumn(0, "000")
        convertToDatabaseColumn(51, "051")
        convertToDatabaseColumn(100, "100")
    }

    @Test
    fun convertToEntityAttribute() {
        convertToEntityAttribute(null, null)
        convertToEntityAttribute("   ", null)
        convertToEntityAttribute("000", 0)
        convertToEntityAttribute("051", 51)
        convertToEntityAttribute("100", 100)
    }

    fun convertToEntityAttribute(input: String?, expected: Int?) {
        val result = converter.convertToEntityAttribute(input)
        assertThat(result).isEqualTo(expected)
    }

    fun convertToDatabaseColumn(input: Int?, expected: String?) {
        val result = converter.convertToDatabaseColumn(input)
        assertThat(result).isEqualTo(expected)
    }
}