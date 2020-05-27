package no.nav.infotrygd.barnetrygd.model.converters

import no.nav.infotrygd.barnetrygd.model.kodeverk.Frisk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class FriskConverterTest {

    private val converter = FriskConverter()

    @Test
    fun convertToDatabaseColumn() {
        assertThat(converter.convertToDatabaseColumn(Frisk.LOPENDE)).isEqualTo(" ")
        assertThat(converter.convertToDatabaseColumn(Frisk.AVVIST)).isEqualTo("A")
    }

    @Test
    fun convertToEntityAttribute() {
        assertThat(converter.convertToEntityAttribute(" ")).isEqualTo(Frisk.LOPENDE)
        assertThat(converter.convertToEntityAttribute("A")).isEqualTo(Frisk.AVVIST)
    }
}