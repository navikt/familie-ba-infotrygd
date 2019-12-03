package no.nav.infotrygd.beregningsgrunnlag.service

import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Stoenadstype
import no.nav.infotrygd.beregningsgrunnlag.testutil.TestData
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner
import java.time.LocalDate


@RunWith(SpringRunner::class)
@ContextConfiguration(classes = [PaaroerendeSykdomGrunnlagService::class])
class PaaroerendeSykdomGrunnlagServiceTest {

    @MockBean
    private lateinit var paaroerendeSykdomISBasenService: PaaroerendeSykdomISBasenService

    @MockBean
    private lateinit var paaroerendeSykdomVedtaksbasenService: PaaroerendeSykdomVedtaksbasenService

    @Autowired
    lateinit var service: PaaroerendeSykdomGrunnlagService

    @Test
    fun service() {
        val dato = LocalDate.of(2019, 1, 1)

        val fnr = TestData.foedselsNr()
        val periode = TestData.periode().copy(
            stoenadstype = Stoenadstype.BARNS_SYKDOM,
            arbufoer = dato,
            stoppdato = dato)
        val vedtak = TestData.vedtak(
            datoStart = dato,
            datoOpphoer = dato)

        val relevanteSaker = listOf(periode, vedtak)

        `when`(paaroerendeSykdomISBasenService.hentPaaroerendeSykdom(fnr))
            .thenReturn(listOf(periode))

        `when`(paaroerendeSykdomVedtaksbasenService.barnsSykdom(fnr))
            .thenReturn(listOf(vedtak))

        assertThat(service.hentPaaroerendeSykdom(fnr, dato, dato)).hasSameSizeAs(relevanteSaker)

        val tildigDato = dato.plusDays(1)
        assertThat(service.hentPaaroerendeSykdom(fnr, tildigDato, tildigDato)).isEmpty()
    }
}