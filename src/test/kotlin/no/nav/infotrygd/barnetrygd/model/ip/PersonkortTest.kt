package no.nav.infotrygd.barnetrygd.model.ip

import no.nav.infotrygd.barnetrygd.testutil.TestData
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

internal class PersonkortTest {

    private val a = LocalDate.of(2020, 1, 1)
    private val b = a.plusMonths(1)
    private val c = b.plusMonths(1)
    private val d = c.plusMonths(1)
    private val e = d.plusMonths(1)

    @Test
    fun fomTomUtenforEllerInniPersonkort() {
        val pk = TestData.personkort(dato = b, fom = b, tom = d)

        assertThat(pk.innenforPeriode(a, a)).isFalse()
        assertThat(pk.innenforPeriode(a, b)).isTrue()
        assertThat(pk.innenforPeriode(a, c)).isTrue()
        assertThat(pk.innenforPeriode(a, e)).isTrue()
        assertThat(pk.innenforPeriode(c, e)).isTrue()
        assertThat(pk.innenforPeriode(c, c)).isTrue()
        assertThat(pk.innenforPeriode(d, e)).isTrue()
        assertThat(pk.innenforPeriode(e, e)).isFalse()
    }

    @Test
    fun fomTomInniPersonkort() {
        val pk = TestData.personkort(dato = a, fom = a, tom = e)
        assertThat(pk.innenforPeriode(b, c)).isTrue()
        assertThat(pk.innenforPeriode(a, e)).isTrue()
    }

    @Test
    fun datoUtenforPersonkort() {
        val pk = TestData.personkort(dato = b, fom = d, tom = e)

        // rundt dato
        assertThat(pk.innenforPeriode(a, c)).isTrue()
        assertThat(pk.innenforPeriode(b, b)).isTrue()

        // Utenfor dato og periode
        assertThat(pk.innenforPeriode(c, c)).isFalse()
        assertThat(pk.innenforPeriode(a, a)).isFalse()

        // rundt periode
        assertThat(pk.innenforPeriode(d, e)).isTrue()
    }

    @Test
    fun `uten tom`() {
        val pk = TestData.personkort(dato = LocalDate.MIN, fom = c, tom = null)
        assertThat(pk.innenforPeriode(a, b)).isFalse()
        assertThat(pk.innenforPeriode(b, c)).isTrue()
        assertThat(pk.innenforPeriode(b, d)).isTrue()
        assertThat(pk.innenforPeriode(d, e)).isTrue()
    }
}