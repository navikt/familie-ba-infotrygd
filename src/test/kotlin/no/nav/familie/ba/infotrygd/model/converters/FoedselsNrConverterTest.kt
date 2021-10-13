package no.nav.familie.ba.infotrygd.model.converters

import no.nav.commons.foedselsnummer.FoedselsNr
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class FoedselsNrConverterTest {

    private val converter = FoedselNrConverter()

    private val str = "01015450572" // TestData.foedselsNr(foedselsdato = LocalDate.of(1854, 1, 1))
    private val fnr = FoedselsNr(str)

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