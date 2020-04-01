package no.nav.infotrygd.beregningsgrunnlag.model.db2

import no.nav.infotrygd.beregningsgrunnlag.testutil.TestData
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

class VedtakTest {

    @Test
    fun innenforPeriode() {
        val start = LocalDate.of(2019, 1, 1)
        val stop = start.plusYears(1)

        val vedtak = TestData.vedtak(datoStart = start, datoOpphoer = stop)

        Assertions.assertThat(vedtak.innenforPeriode(LocalDate.MIN, null)).isTrue()
        Assertions.assertThat(vedtak.innenforPeriode(LocalDate.MIN, start.minusDays(1))).isFalse()
        Assertions.assertThat(vedtak.innenforPeriode(LocalDate.MIN, start)).isTrue()

        Assertions.assertThat(vedtak.innenforPeriode(start.minusDays(1), start.plusDays(1))).isTrue()

        Assertions.assertThat(vedtak.innenforPeriode(start, stop)).isTrue()
        Assertions.assertThat(vedtak.innenforPeriode(start.plusDays(1), stop.minusDays(1))).isTrue()

        Assertions.assertThat(vedtak.innenforPeriode(stop.minusDays(1), null)).isTrue()
        Assertions.assertThat(vedtak.innenforPeriode(stop.minusDays(1), stop.plusDays(1))).isTrue()

        Assertions.assertThat(vedtak.innenforPeriode(stop, stop.plusDays(1))).isTrue()
        Assertions.assertThat(vedtak.innenforPeriode(stop.plusDays(1), null)).isFalse()
        Assertions.assertThat(vedtak.innenforPeriode(stop.plusDays(1), stop.plusDays(2))).isFalse()
    }

    @Test
    fun innenforPeriodeOpphoerNull() {
        val start = LocalDate.of(2019, 1, 1)
        val vedtak = TestData.vedtak(datoStart = start, datoOpphoer = null)
        Assertions.assertThat(vedtak.innenforPeriode(start, start.plusDays(1))).isTrue()
    }


    @Test
    fun opphoerFom_gt_tom() {
        val tom = LocalDate.of(2020, 1, 1)
        val opphoerFom = tom.plusDays(1)
        assertThat(vedtakMedDelytelse(tom, opphoerFom).annullert).isFalse()
    }

    @Test
    fun opphoerFom_eq_tom() {
        val dato = LocalDate.of(2020, 1, 1)
        assertThat(vedtakMedDelytelse(dato, dato).annullert).isTrue()
    }

    @Test
    fun opphoerFom_lt_tom() {
        val opphoerFom = LocalDate.of(2020, 1, 1)
        val tom = opphoerFom.plusDays(1)
        assertThat(vedtakMedDelytelse(tom, opphoerFom).annullert).isTrue()
    }

    @Test
    fun opphoerFom_null() {
        val dato = LocalDate.of(2020, 1, 1)
        assertThat(vedtakMedDelytelse(dato, null).annullert).isFalse()
    }

    fun vedtakMedDelytelse(tom: LocalDate, opphoerFom: LocalDate?): Vedtak {
        return TestData.vedtak(vedtakSpFaBsOpphoer = opphoerFom, delytelserEksermpler = listOf(
            TestData.delytelse().copy(tom = tom)
        ))
    }
}