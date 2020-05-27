package no.nav.infotrygd.barnetrygd.model.converters

import no.nav.infotrygd.barnetrygd.model.converters.AbstractNavLocalDateConverter.Companion.NULL_VALUE
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

class NavReversedLocalDateConverterTest {
    private val converter = NavReversedLocalDateConverter()

    @Test
    fun convertToDatabaseColumn() {
        val result: Int? = converter.convertToDatabaseColumn(LocalDate.of(2019, 1, 1))
        assertThat(result).isEqualTo(1012019)

        assertThat(converter.convertToDatabaseColumn(null)).isNull()
    }

    @Test
    fun convertToEntityAttribute() {
        val result: LocalDate? = converter.convertToEntityAttribute(1012019)
        assertThat(result).isEqualTo(LocalDate.of(2019, 1, 1))

        assertThat(converter.convertToEntityAttribute(null)).isNull()
        assertThat(converter.convertToEntityAttribute(NULL_VALUE)).isNull()
    }
}
