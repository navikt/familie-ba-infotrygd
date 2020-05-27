package no.nav.infotrygd.barnetrygd.model.db2

import no.nav.infotrygd.barnetrygd.testutil.TestData
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class StonadTest {
    @Test
    fun inntekter() {
        val relevantOrgNr: Long = 2

        val stonad = TestData.stonad(TestData.stonadBs()).copy(
            inntektshistorikk = listOf(
                TestData.inntektStonad().copy(
                    status = "x",
                    orgNr = 1
                ),
                TestData.inntektStonad().copy(
                    status = "L",
                    orgNr = relevantOrgNr
                )
            )
        )

        assertThat(stonad.inntekter).hasSize(1)
        assertThat(stonad.inntekter[0].orgNr).isEqualTo(relevantOrgNr)
    }
}