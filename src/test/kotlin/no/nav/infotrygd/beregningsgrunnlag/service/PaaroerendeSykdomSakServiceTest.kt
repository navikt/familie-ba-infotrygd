package no.nav.infotrygd.beregningsgrunnlag.service

import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.SakValg
import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Stoenadstype
import no.nav.infotrygd.beregningsgrunnlag.repository.PeriodeRepository
import no.nav.infotrygd.beregningsgrunnlag.repository.SakRepository
import no.nav.infotrygd.beregningsgrunnlag.repository.VedtakRepository
import no.nav.infotrygd.beregningsgrunnlag.testutil.TestData
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import java.time.LocalDate

@RunWith(SpringRunner::class)
@SpringBootTest
@ActiveProfiles("test")
internal class PaaroerendeSykdomSakServiceTest {

    @Autowired
    private lateinit var paaroerendeSykdomSakService: PaaroerendeSykdomSakService

    @Autowired
    private lateinit var periodeRepository: PeriodeRepository

    @Autowired
    private lateinit var vedtakRepository: VedtakRepository

    @Autowired
    private lateinit var sakRepository: SakRepository

    @Test
    fun hentSak() {
        val datoStart = LocalDate.now()

        val fnr = TestData.foedselsNr()
        val periode = TestData.periode().copy(
            fnr = fnr,
            stoenadstype = Stoenadstype.BARNS_SYKDOM,
            arbufoerTom = datoStart
        )

        periodeRepository.save(periode)

        val vedtak = TestData.vedtak(kodeRutine = "BS", datoStart = datoStart, fnr = fnr)
        vedtakRepository.save(vedtak)

        val sak = TestData.sak(fnr = fnr).copy(
            kapittelNr = "BS",
            valg = SakValg.OM
        )
        sakRepository.save(sak)

        val fom = datoStart.minusDays(1)
        val tom = datoStart.plusDays(1)

        val res = paaroerendeSykdomSakService.hentSak(fnr, fom, tom)
        assertThat(res).hasSize(3)
    }
}