package no.nav.infotrygd.beregningsgrunnlag.model

import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Stoenadstype
import no.nav.infotrygd.beregningsgrunnlag.values.FodselNr
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

    private fun periode(type: Stoenadstype): Periode {
        val p = Periode(
            id = 0,
            personKey = 0,
            arbufoerSeq = 0,
            stoenadstype = type,
            fnr = FodselNr("12345678900"),
            frisk = null,
            arbufoer = LocalDate.now(),
            stoppdato = LocalDate.now(),
            utbetalinger = listOf(),
            inntekter = listOf(),
            utbetaltFom = null,
            utbetaltTom = null,
            arbufoerOpprinnelig = LocalDate.now(),
            dekningsgrad = null,
            foedselsdatoBarn = null,
            arbeidskategori = null
        )
        return p
    }
}