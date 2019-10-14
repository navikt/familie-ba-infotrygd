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
        // 000-499 // 1900-1999
        testFdato("01020000000", 1900)
        testFdato("01020049900", 1900)
        testFdato("01029900000", 1999)
        testFdato("01029949900", 1999)

        // 500-999, overlapper alt under // 2000-2039
        testFdato("01020050000", 2000)  // 500: overlapp 1854-1899
        testFdato("01020074900", 2000)  // 749: overlapp 1854-1899
        testFdato("01020090000", 2000)  // 900: overlapp 1940-1999
        testFdato("01020099900", 2000)  // 999: overlapp 1940-1999
        testFdato("01023950000", 2039)  // 500: overlapp 1854-1899
        testFdato("01023974900", 2039)  // 749: overlapp 1854-1899
        testFdato("01023990000", 2039)  // 900: overlapp 1940-1999
        testFdato("01023999900", 2039)  // 999: overlapp 1940-1999

        // 500-749, overlapp // 1854-1899
        testFdato("01025450000", 1854)  // 500: overlapp 2000-2039
        testFdato("01025474900", 1854)  // 749: overlapp 2000-2039
        testFdato("01029950000", 1899)  // 500: overlapp 2000-2039
        testFdato("01029974900", 1899)  // 749: overlapp 2000-2039

        // 900-999, overlapp // 1940-1999
        testFdato("01024090000", 1940)  // 900: overlapp 2000-2039
        testFdato("01024099900", 1940)  // 999: overlapp 2000-2039
        testFdato("01029990000", 1999)  // 900: overlapp 2000-2039
        testFdato("01029999900", 1999)  // 999: overlapp 2000-2039
    }

    fun testFdato(fnr: String, year: Int) {
        val dato = LocalDate.of(year, 2, 1)
        assertThat(FoedselNr(fnr).foedselsdato).isEqualTo(dato)
    }

    @Test
    fun foedseldatoDnummer() {
        val fnr = FoedselNr("41021599945")
        val dato = LocalDate.of(2015, 2, 1)

        assertThat(fnr.foedselsdato).isEqualTo(dato)
    }
}