package no.nav.infotrygd.beregningsgrunnlag.service

import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Stoenadstype
import no.nav.infotrygd.beregningsgrunnlag.repository.PeriodeRepository
import no.nav.infotrygd.beregningsgrunnlag.testutil.TestData
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import java.time.LocalDate

@SpringBootTest
@RunWith(SpringRunner::class)
@ActiveProfiles("test")
class SykepengerServiceTest {

    @Autowired
    private lateinit var sykepengerService: SykepengerService

    @Autowired
    private lateinit var periodeRepository: PeriodeRepository

    @Test
    fun hentSykepenger() {
        val factory = TestData.PeriodeFactory()

        val prosent = 90

        val periode = factory.periode().copy(
            stoenadstype = Stoenadstype.SYKEPENGER,
            inntektsgrunnlagProsent = prosent
        )

        periodeRepository.save(periode)

        val resultat = sykepengerService.hentSykepenger(factory.fnr, LocalDate.now().minusYears(1), null)
        assertThat(resultat).hasSize(1)

        val grunnlag = resultat[0]
        assertThat(grunnlag.inntektsgrunnlagProsent).isEqualTo(prosent)
    }
}