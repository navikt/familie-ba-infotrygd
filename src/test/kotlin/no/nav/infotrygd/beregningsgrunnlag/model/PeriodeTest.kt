package no.nav.infotrygd.beregningsgrunnlag.model

import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Stoenadstype
import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Tema
import no.nav.infotrygd.beregningsgrunnlag.testutil.TestData
import no.nav.infotrygd.beregningsgrunnlag.values.FoedselNr
import no.nav.infotrygd.beregningsgrunnlag.values.Kjoenn
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


class PeriodeTest {
    @Test
    fun getYtelse() {
        assertThat(periode(Stoenadstype.SYKEPENGER).tema).isEqualTo(Tema.SYKEPENGER)

        val foreldrepenger = listOf(
            Stoenadstype.FOEDSEL,
            Stoenadstype.ADOPSJON,
            Stoenadstype.RISIKOFYLT_ARBMILJOE,
            Stoenadstype.SVANGERSKAP)

        for(type in foreldrepenger) {
            assertThat(periode(type).tema).isEqualTo(Tema.FORELDREPENGER)
        }

        val paaroerendeSykdom = listOf(
            Stoenadstype.BARNS_SYKDOM,
            Stoenadstype.ALV_SYKT_BARN,
            Stoenadstype.KURS_KAP_3_23,
            Stoenadstype.PAS_DOEDSSYK,
            Stoenadstype.PLEIEPENGER_INSTOPPH,
            Stoenadstype.PLEIEPENGER_NY_ORDNING
        )

        for(type in paaroerendeSykdom) {
            assertThat(periode(type).tema).isEqualTo(Tema.PAAROERENDE_SYKDOM)
        }

        // TODO:
        // T_STONAD og T_VEDTAK: felt KODE_RUTINE
        //
        //'BS' - Barn Sykdom
        //'BR' - Barn Sykdom m/refusjon
    }

    @Test
    fun barnPersonKey() {
        val periode = TestData.periode().copy(
            tkNr = "1000",
            barnFnr = FoedselNr("01109912345")
        )

        assertThat(periode.barnPersonKey).isEqualTo("100099100112345".toLong())
    }

    @Test
    fun barnKode() {
        for(adopsjon in listOf("A", "D")) {
            for(kjoenn in Kjoenn.values()) {
                val kode = beregnKode(adopsjon, kjoenn)
                assertThat(kode).isEqualTo("1")
            }
        }

        for(adopsjon in listOf("B", "C", "E")) {
            for(kjoenn in Kjoenn.values()) {
                val kode = beregnKode(adopsjon, kjoenn)
                assertThat(kode).isEqualTo("2")
            }
        }

        assertThat(beregnKode("x", Kjoenn.KVINNE)).isEqualTo("1")
        assertThat(beregnKode("x", Kjoenn.MANN)).isEqualTo("2")
    }

    private fun beregnKode(adopsjon: String, kjoenn: Kjoenn): String? {
        val mannFnr = FoedselNr("00000000100")
        assertThat(mannFnr.kjoenn).isEqualTo(Kjoenn.MANN)

        val kvinneFnr = FoedselNr("00000000200")
        assertThat(kvinneFnr.kjoenn).isEqualTo(Kjoenn.KVINNE)

        return TestData.periode().copy(
            stebarnsadopsjon = adopsjon,
            fnr = if (kjoenn == Kjoenn.MANN) mannFnr else kvinneFnr
        ).barnKode
    }

    private fun periode(type: Stoenadstype): Periode {
        return TestData.periode().copy(
            stoenadstype = type
        )
    }
}