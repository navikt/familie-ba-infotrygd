package no.nav.infotrygd.beregningsgrunnlag.service

import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Stoenadstype
import no.nav.infotrygd.beregningsgrunnlag.repository.PeriodeRepository
import no.nav.infotrygd.beregningsgrunnlag.repository.VedtakBarnRepository
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
class SvangerskapspengerServiceTest {

    @Autowired
    private lateinit var svangerskapspengerService: SvangerskapspengerService

    @Autowired
    private lateinit var periodeRepository: PeriodeRepository

    @Autowired
    private lateinit var vedtakBarnRepository: VedtakBarnRepository

    @Test
    fun hentSvangerskapspenger() {
        val gradering = 10

        val factory = TestData.PeriodeFactory()

        val inntekt = factory.inntekt()
        val utbetaling = factory.utbetaling()

        val brukerId = "bruker"
        val periode = factory.periode().copy(
            stoenadstype = Stoenadstype.SVANGERSKAP,
            brukerId = brukerId,
            foedselsdatoBarn = LocalDate.now().minusYears(1),
            inntekter = listOf(inntekt),
            utbetalinger = listOf(utbetaling)
        )

        periodeRepository.save(periode)

        val vedtak = factory.vedtakBarn().copy(
            dekningsgrad = gradering
        )
        vedtakBarnRepository.save(vedtak)

        val resultat =
            svangerskapspengerService.hentSvangerskapspenger(factory.fnr, LocalDate.now().minusYears(1), null)

        println(resultat)

        assertThat(resultat).hasSize(1)
        val fp = resultat[0]

        assertThat(fp.gradering).isEqualTo(gradering)
        assertThat(fp.vedtak).hasSize(1)
        assertThat(fp.arbeidsforhold).hasSize(1)
        assertThat(fp.saksbehandlerId).isEqualTo(brukerId)
    }
}