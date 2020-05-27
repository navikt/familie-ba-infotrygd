package no.nav.infotrygd.barnetrygd.service

import no.nav.infotrygd.barnetrygd.model.kodeverk.SakValg
import no.nav.infotrygd.barnetrygd.model.kodeverk.Stoenadstype
import no.nav.infotrygd.barnetrygd.repository.PeriodeRepository
import no.nav.infotrygd.barnetrygd.repository.SakRepository
import no.nav.infotrygd.barnetrygd.repository.VedtakRepository
import no.nav.infotrygd.barnetrygd.testutil.TestData
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
        val dato = LocalDate.now()

        val fnr = TestData.foedselsNr()
        val periode = TestData.periode().copy(
            fnr = fnr,
            stoenadstype = Stoenadstype.BARNS_SYKDOM,
            arbufoerTom = dato,
            stoppdato = dato
        )

        periodeRepository.save(periode)

        val vedtak = TestData.vedtak(kodeRutine = "BS", datoStart = dato, datoOpphoer = dato, fnr = fnr)
        vedtakRepository.save(vedtak)

        val sak = TestData.sak(fnr = fnr).copy(
            kapittelNr = "BS",
            valg = SakValg.OM,
            registrert = dato
        )
        sakRepository.save(sak)

        val vedtaksliste = listOf(periode, vedtak)
        val saksliste = listOf(sak)

        val fom = dato.minusDays(1)
        val tom = dato.plusDays(1)

        val res = paaroerendeSykdomSakService.hentSak(fnr, fom, tom)
        assertThat(res.saker).hasSameSizeAs(saksliste)
        assertThat(res.vedtak).hasSameSizeAs(vedtaksliste)

        val tomResponse = paaroerendeSykdomSakService.hentSak(fnr, dato.minusDays(1), dato.minusDays(1))
        assertThat(tomResponse.saker).isEmpty()
        assertThat(tomResponse.vedtak).isEmpty()
    }
}