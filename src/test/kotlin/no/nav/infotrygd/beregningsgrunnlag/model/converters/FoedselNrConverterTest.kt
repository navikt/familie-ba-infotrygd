package no.nav.infotrygd.beregningsgrunnlag.model.converters

import no.nav.infotrygd.beregningsgrunnlag.values.FoedselNr
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class FoedselNrConverterTest {

    private val converter = FoedselNrConverter()

    private val str = "12345678900"
    private val fnr = FoedselNr(str)

    @Test
    fun convertToDatabaseColumn() {
        assertThat(converter.convertToDatabaseColumn(fnr)).isEqualTo(str)
        assertThat(converter.convertToDatabaseColumn(null)).isNull()
    }

    @Test
    fun convertToEntityAttribute() {
        assertThat(converter.convertToEntityAttribute(str)).isEqualTo(fnr)
        assertThat(converter.convertToEntityAttribute(null)).isNull()
    }
}