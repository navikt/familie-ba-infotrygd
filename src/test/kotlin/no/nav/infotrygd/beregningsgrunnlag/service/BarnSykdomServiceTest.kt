package no.nav.infotrygd.beregningsgrunnlag.service

import no.nav.infotrygd.beregningsgrunnlag.model.db2.Vedtak
import no.nav.infotrygd.beregningsgrunnlag.repository.VedtakRepository
import no.nav.infotrygd.beregningsgrunnlag.testutil.TestData
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
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
class BarnSykdomServiceTest {

    @Autowired
    lateinit var service: BarnSykdomService

    @Autowired
    lateinit var vedtakRepository: VedtakRepository

    val datoFoerste = LocalDate.now()
    val fnr = TestData.foedselNr()
    val datoAndre = datoFoerste.plusMonths(1)

    lateinit var foerste: Vedtak

    lateinit var andre: Vedtak

    @Before
    fun setUp() {
        foerste = TestData.vedtak(
            datoStart = datoFoerste,
            fnr = fnr,
            delytelserEksermpler = listOf(TestData.delytelse())
        )

        andre = TestData.vedtak(
            datoStart = datoAndre,
            fnr = fnr,
            delytelserEksermpler = listOf(TestData.delytelse())
        )

        vedtakRepository.saveAll(listOf(foerste, andre))
    }

    @Test
    fun barnsSykdomFom() {
        val resultat = service.barnsSykdom(fnr, datoFoerste, null)
        assertThat(resultat).hasSize(2)
    }

    @Test
    fun barnsSykdomFomTom() {
        val resultat = service.barnsSykdom(fnr, datoFoerste, datoFoerste.plusDays(1))
        assertThat(resultat).hasSize(1)
    }
}