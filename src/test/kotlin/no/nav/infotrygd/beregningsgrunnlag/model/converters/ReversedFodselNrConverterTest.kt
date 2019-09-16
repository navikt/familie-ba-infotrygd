package no.nav.infotrygd.beregningsgrunnlag.model.converters

import no.nav.infotrygd.beregningsgrunnlag.values.FodselNr
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

internal class ReversedFodselNrConverterTest {

    private val dbValue = "87020112345"
    private val entity = FodselNr("01028712345")

    private val converter = ReversedFodselNrConverter()

    @Test
    fun convertToDatabaseColumn() {
        val result = converter.convertToDatabaseColumn(entity)
        assertThat(result).isEqualTo(dbValue)
        assertThat(converter.convertToDatabaseColumn(null)).isNull()
    }

    @Test
    fun convertToEntityAttribute() {
        val result = converter.convertToEntityAttribute(dbValue)
        assertThat(result).isEqualTo(entity)
        assertThat(converter.convertToEntityAttribute(null)).isNull()
    }
}