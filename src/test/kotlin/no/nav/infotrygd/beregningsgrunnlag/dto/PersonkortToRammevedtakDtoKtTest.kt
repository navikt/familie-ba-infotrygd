package no.nav.infotrygd.beregningsgrunnlag.dto

import no.nav.infotrygd.beregningsgrunnlag.testutil.TestData
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

class PersonkortToRammevedtakDtoKtTest {

    @Test
    fun personkortToRammevedtakDto() {

        val dato = LocalDate.of(2020, 1, 1)
        val fom = dato.plusMonths(1)
        val tom = fom.plusMonths(1)

        val personkort = TestData.personkort(
            tekst = "tekst   ",
            dato = dato,
            fom = fom,
            tom = tom)

        val resultat = personkortToRammevedtakDto(personkort)

        val forventet = RammevedtakDto(
            tekst = "tekst",
            fom = fom,
            tom = tom,
            date = dato
        )

        assertThat(resultat).isEqualTo(forventet)
    }
}