package no.nav.infotrygd.beregningsgrunnlag.model.db2

import no.nav.infotrygd.beregningsgrunnlag.testutil.TestData
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class StonadTest {
    @Test
    fun inntekter() {
        val relevantOrgNr: Long = 2

        val stonad = TestData.stonad().copy(
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