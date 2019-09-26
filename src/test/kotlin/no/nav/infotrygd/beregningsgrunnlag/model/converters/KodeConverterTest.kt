package no.nav.infotrygd.beregningsgrunnlag.model.converters

import no.nav.infotrygd.beregningsgrunnlag.exception.UkjentDatabaseverdiException
import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Kode
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


class KodeConverterTest {

    private val converter = TestConverter()

    @Test
    fun convertToDatabaseColumn() {
        assertThat(converter.convertToDatabaseColumn(E.B)).isEqualTo("b ")
        assertThat(converter.convertToDatabaseColumn(null)).isNull()
    }

    @Test
    fun convertToEntityAttribute() {
        assertThat(converter.convertToEntityAttribute("b")).isEqualTo(E.B)
        assertThat(converter.convertToEntityAttribute("b ")).isEqualTo(E.B)
        assertThat(converter.convertToEntityAttribute(null)).isNull()
        assertThat(converter.convertToEntityAttribute("  ")).isNull()
    }

    @Test(expected = UkjentDatabaseverdiException::class)
    fun convertToEntityAttribute_Exception() {
        converter.convertToEntityAttribute("XX")
    }

    private enum class E(override val kode: String, override val tekst: String) : Kode {
        A("a", "aaa"),
        B("b", "bbb")
    }

    private class TestConverter : KodeConverter<E>(E.values().toList(), fieldSize = 2)
}