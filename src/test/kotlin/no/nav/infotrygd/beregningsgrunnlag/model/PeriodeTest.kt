package no.nav.infotrygd.beregningsgrunnlag.model

import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Stoenadstype
import no.nav.infotrygd.beregningsgrunnlag.testutil.TestData
import no.nav.infotrygd.beregningsgrunnlag.values.FodselNr
import no.nav.infotrygd.beregningsgrunnlag.values.Kjoenn
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate


class PeriodeTest {
    @Test
    fun getYtelse() {
        assertThat(periode(Stoenadstype.SYKEPENGER).ytelse).isEqualTo(Ytelse.SYKEPENGER)

        val foreldrepenger = listOf(
            Stoenadstype.FOEDSEL,
            Stoenadstype.ADOPSJON,
            Stoenadstype.RISIKOFYLT_ARBMILJOE,
            Stoenadstype.SVANGERSKAP)

        for(type in foreldrepenger) {
            assertThat(periode(type).ytelse).isEqualTo(Ytelse.FORELDREPENGER)
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
            assertThat(periode(type).ytelse).isEqualTo(Ytelse.PAAROERENDE_SYKDOM)
        }

        // TODO:
        // T_STONAD og T_VEDTAK: felt KODE_RUTINE
        //
        //'BS' - Barn Sykdom
        //'BR' - Barn Sykdom m/refusjon
    }

    @Test
    fun barnPersonKey() {

        val tkNr = "1000"
        val barnFnr = "10101012345"

        val periode = TestData.periode().copy(
            tkNr = tkNr,
            tidskontoBarnFnr = barnFnr
            // todo: tknr, barnfnr, adopsjon
        )

        assertThat(periode.barnPersonKey).isEqualTo("$tkNr$barnFnr".toLong())
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
        val mannFnr = FodselNr("00000000100")
        assertThat(mannFnr.kjoenn).isEqualTo(Kjoenn.MANN)

        val kvinneFnr = FodselNr("00000000200")
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