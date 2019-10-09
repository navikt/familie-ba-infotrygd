package no.nav.infotrygd.beregningsgrunnlag.model.converters

import no.nav.infotrygd.beregningsgrunnlag.values.FoedselNr
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

internal class ReversedFoedselNrConverterTest {

    private val dbValue = "87020112345"
    private val entity = FoedselNr("01028712345")

    private val converter = ReversedFoedselNrConverter()

    @Test
    fun convertToDatabaseColumn() {
        val result = converter.convertToDatabaseColumn(entity)
        assertThat(result).isEqualTo(dbValue)
        assertThat(converter.convertToDatabaseColumn(null)).isEqualTo("00000000000")
    }

    @Test
    fun convertToEntityAttribute() {
        val result = converter.convertToEntityAttribute(dbValue)
        assertThat(result).isEqualTo(entity)
        assertThat(converter.convertToEntityAttribute(null)).isNull()
        assertThat(converter.convertToEntityAttribute("00000000000")).isNull()
    }
}