package no.nav.familie.ba.infotrygd.model.converters

import no.nav.familie.ba.infotrygd.model.converters.AbstractNavLocalDateConverter.Companion.NULL_VALUE
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class NavLocalDateConverterTest {
    private val converter = NavLocalDateConverter()

    @Test
    fun convertToDatabaseColumn() {
        val result: Int? = converter.convertToDatabaseColumn(LocalDate.of(2019, 1, 1))
        assertThat(result).isEqualTo(20190101)

        assertThat(converter.convertToDatabaseColumn(null)).isNull()
    }

    @Test
    fun convertToEntityAttribute() {
        val result: LocalDate? = converter.convertToEntityAttribute(20190101)
        assertThat(result).isEqualTo(LocalDate.of(2019, 1, 1))

        assertThat(converter.convertToEntityAttribute(null)).isNull()
        assertThat(converter.convertToEntityAttribute(NULL_VALUE)).isNull()
    }
}