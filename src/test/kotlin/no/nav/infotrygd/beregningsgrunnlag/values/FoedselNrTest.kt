package no.nav.infotrygd.beregningsgrunnlag.values

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

class FoedselNrTest {
    @Test
    fun kjoenn() {
        val mann = FoedselNr("00000000100")
        val kvinne = FoedselNr("00000000200")

        assertThat(mann.kjoenn).isEqualTo(Kjoenn.MANN)
        assertThat(kvinne.kjoenn).isEqualTo(Kjoenn.KVINNE)
    }

    @Test
    fun foedselsdato() {
        val fnr = FoedselNr("01021512345")
        val dato2015 = LocalDate.of(2015, 2, 1)
        val datoEtter2015 = dato2015.plusYears(10)
        assertThat(fnr.finnSisteMuligeFoedselsdatoFoer(datoEtter2015)).isEqualTo(dato2015)

        val dato1915 = LocalDate.of(1915, 2, 1)
        val datoEtter1915 = dato1915.plusYears(5)
        assertThat(fnr.finnSisteMuligeFoedselsdatoFoer(datoEtter1915)).isEqualTo(dato1915)
    }

    @Test
    fun foedseldatoDnummer() {
        val fnr = FoedselNr("41021512345")
        val dato2015 = LocalDate.of(2015, 2, 1)
        val datoEtter2015 = dato2015.plusYears(10)
        assertThat(fnr.finnSisteMuligeFoedselsdatoFoer(datoEtter2015)).isEqualTo(dato2015)
    }
}