package no.nav.infotrygd.beregningsgrunnlag.model

import no.nav.infotrygd.beregningsgrunnlag.testutil.TestData
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

class SakTest {
    @Test
    fun innenforPeriode() {
        val registrert = LocalDate.of(2019, 1, 1)
        val sak = TestData.sak().copy(
            registrert = registrert
        )

        assertThat(sak.innenforPeriode(LocalDate.MIN, null)).isTrue()
        assertThat(sak.innenforPeriode(LocalDate.MIN, registrert.minusDays(1))).isFalse()
        assertThat(sak.innenforPeriode(LocalDate.MIN, registrert)).isTrue()

        assertThat(sak.innenforPeriode(registrert, registrert)).isTrue()
        assertThat(sak.innenforPeriode(registrert.minusDays(1), registrert.plusDays(1))).isTrue()

        assertThat(sak.innenforPeriode(registrert, null)).isTrue()
        assertThat(sak.innenforPeriode(registrert, registrert.plusDays(1))).isTrue()

        assertThat(sak.innenforPeriode(registrert.plusDays(1), null)).isFalse()
        assertThat(sak.innenforPeriode(registrert.plusDays(1), registrert.plusDays(2))).isFalse()
    }
}