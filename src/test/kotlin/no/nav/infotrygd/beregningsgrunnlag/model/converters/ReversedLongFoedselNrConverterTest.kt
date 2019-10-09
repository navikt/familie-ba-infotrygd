package no.nav.infotrygd.beregningsgrunnlag.model.converters

import no.nav.infotrygd.beregningsgrunnlag.values.FoedselNr
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ReversedLongFoedselNrConverterTest {

    private val converter = ReversedLongFoedselNrConverter()

    @Test
    fun convertToDatabaseColumn() {
        assertThat(converter.convertToDatabaseColumn(null)).isEqualTo(0)
        assertThat(converter.convertToDatabaseColumn(FoedselNr("01019912345"))).isEqualTo(99010112345)
    }

    @Test
    fun convertToEntityAttribute() {
        val full  = 99010112345L
        val short =  1010112345L

        assertThat(converter.convertToEntityAttribute(full)).isEqualTo(FoedselNr("01019912345"))
        assertThat(converter.convertToEntityAttribute(short)).isEqualTo(FoedselNr("01010112345"))
        assertThat(converter.convertToEntityAttribute(0)).isNull()
    }
}