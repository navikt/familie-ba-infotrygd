package no.nav.infotrygd.beregningsgrunnlag.model

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.commons.foedselsnummer.Kjoenn
import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Stoenadstype
import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Tema
import no.nav.infotrygd.beregningsgrunnlag.testutil.TestData
import no.nav.infotrygd.beregningsgrunnlag.utils.reversert
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
        val tkNr = "1000"
        val fnr = TestData.foedselsNr()

        val periode = TestData.periode().copy(
            tkNr = tkNr,
            barnFnr = fnr
        )

        assertThat(periode.barnPersonKey).isEqualTo("$tkNr${fnr.reversert}".toLong())
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
        val fnr = TestData.foedselsNr(kjoenn = kjoenn)

        return TestData.periode().copy(
            stebarnsadopsjon = adopsjon,
            fnr = fnr
        ).barnKode
    }

    private fun periode(type: Stoenadstype): Periode {
        return TestData.periode().copy(
            stoenadstype = type
        )
    }
}