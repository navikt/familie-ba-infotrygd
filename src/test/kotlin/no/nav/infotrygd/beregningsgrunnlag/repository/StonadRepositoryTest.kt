package no.nav.infotrygd.beregningsgrunnlag.repository

import no.nav.infotrygd.beregningsgrunnlag.model.db2.Inntekt
import no.nav.infotrygd.beregningsgrunnlag.model.db2.Stonad
import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Inntektsperiode
import no.nav.infotrygd.beregningsgrunnlag.testutil.TestData
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import java.time.LocalDate

@RunWith(SpringRunner::class)
@DataJpaTest
@ActiveProfiles("test")
class StonadRepositoryTest {
    @Autowired
    lateinit var repostitory: StonadRepostitory

    @Test
    fun name() {
        var stonad = TestData.stonad()

        val inntekt1 = inntekt(stonad, 1)
        val inntekt2 = inntekt(stonad, 2)
        stonad = stonad.copy(inntektshistorikk = listOf(inntekt1, inntekt2))

        repostitory.save(stonad)

        val res = repostitory.findById(stonad.id).get()
        assertThat(res.inntekter).hasSize(1)
    }

    private fun inntekt(stonad: Stonad, lopeNr: Long): Inntekt {
        val inntekt = Inntekt(
            stonadId = stonad.id,
            orgNr = 123,
            inntektFom = LocalDate.now().minusYears(1),
            lopeNr = lopeNr,
            inntekt = 123.toBigDecimal(),
            periode = Inntektsperiode.MAANEDLIG,
            status = "L"
        )
        return inntekt
    }
}