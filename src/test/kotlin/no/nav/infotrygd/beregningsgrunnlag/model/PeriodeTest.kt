package no.nav.infotrygd.beregningsgrunnlag.model

import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Stoenadstype
import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Tema
import no.nav.infotrygd.beregningsgrunnlag.testutil.TestData
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate


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
    }

    @Test
    fun opphoerFom() {
        var periode = TestData.periode()
        assertThat(periode.opphoerFom).isNull()

        val stoppdato = LocalDate.of(2019, 1, 1)
        val friskmeldtDato = stoppdato.plusMonths(1)
        val arbufoerTom = friskmeldtDato.plusMonths(1)
        val maksdato = arbufoerTom.plusMonths(1)

        periode = periode.copy(maksdato = maksdato)
        assertThat(periode.opphoerFom).isEqualTo(maksdato)

        periode = periode.copy(arbufoerTom = arbufoerTom)
        assertThat(periode.opphoerFom).isEqualTo(arbufoerTom.plusDays(1))

        periode = periode.copy(friskmeldtDato = friskmeldtDato)
        assertThat(periode.opphoerFom).isEqualTo(friskmeldtDato)

        periode = periode.copy(stoppdato = stoppdato)
        assertThat(periode.opphoerFom).isEqualTo(stoppdato)
    }

    @Test
    fun innenforPeriode() {
        val start = LocalDate.of(2019, 1, 1)
        val stop = start.plusYears(1)

        val periode = TestData.periode().copy(
            arbufoer = start,
            stoppdato = stop
        )

        assertThat(periode.innenforPeriode(LocalDate.MIN, null)).isTrue()
        assertThat(periode.innenforPeriode(LocalDate.MIN, start.minusDays(1))).isFalse()
        assertThat(periode.innenforPeriode(LocalDate.MIN, start)).isTrue()

        assertThat(periode.innenforPeriode(start.minusDays(1), start.plusDays(1))).isTrue()

        assertThat(periode.innenforPeriode(start, stop)).isTrue()
        assertThat(periode.innenforPeriode(start.plusDays(1), stop.minusDays(1))).isTrue()

        assertThat(periode.innenforPeriode(stop.minusDays(1), null)).isTrue()
        assertThat(periode.innenforPeriode(stop.minusDays(1), stop.plusDays(1))).isTrue()

        assertThat(periode.innenforPeriode(stop, stop.plusDays(1))).isTrue()
        assertThat(periode.innenforPeriode(stop.plusDays(1), null)).isFalse()
        assertThat(periode.innenforPeriode(stop.plusDays(1), stop.plusDays(2))).isFalse()
    }

    @Test
    fun utbetalinger() {
        assertThat(periodeMedUtbetalinger("1", "", LocalDate.now()).utbetalinger).isNotEmpty()

        assertThat(periodeMedUtbetalinger("1", "KORR", LocalDate.now()).utbetalinger).isEmpty()
        assertThat(periodeMedUtbetalinger("7", "", LocalDate.now()).utbetalinger).isEmpty()
        assertThat(periodeMedUtbetalinger("1", "", null).utbetalinger).isEmpty()
    }

    private fun periodeMedUtbetalinger(type: String?, korr: String?, utbetalingsdato: LocalDate?): Periode {
        val pf = TestData.PeriodeFactory()
        val utbetaling = pf.utbetaling().copy(
            type = type,
            korr = korr,
            utbetalingsdato = utbetalingsdato
        )
        return pf.periode().copy(
            utbetalingshistorikk = listOf(utbetaling)
        )
    }

    private fun periode(type: Stoenadstype): Periode {
        return TestData.periode().copy(
            stoenadstype = type
        )
    }
}