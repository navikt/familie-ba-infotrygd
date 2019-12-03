package no.nav.infotrygd.beregningsgrunnlag.model.db2

import no.nav.infotrygd.beregningsgrunnlag.testutil.TestData
import org.assertj.core.api.Assertions
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
}