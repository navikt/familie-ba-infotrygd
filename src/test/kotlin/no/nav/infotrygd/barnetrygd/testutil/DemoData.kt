package no.nav.infotrygd.barnetrygd.testutil

import no.nav.infotrygd.barnetrygd.model.kodeverk.Stoenadstype
import no.nav.infotrygd.barnetrygd.repository.PeriodeRepository
import no.nav.infotrygd.barnetrygd.repository.SakRepository
import no.nav.infotrygd.barnetrygd.repository.VedtakRepository
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
@Profile("demoData")
class DemoData(
    private val periodeRepository: PeriodeRepository,
    private val vedtakRepository: VedtakRepository,
    private val sakRepository: SakRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @PostConstruct
    fun init() {
        val fnr = TestData.foedselsNr()

        val pf = TestData.PeriodeFactory(fnr = fnr)
        val periode = pf.periode().copy(
            stoenadstype = Stoenadstype.BARNS_SYKDOM
        )
        periodeRepository.save(periode)

        val vedtak = TestData.vedtak(fnr = fnr)
        vedtakRepository.save(vedtak)

        val sak = TestData.sak(fnr = fnr)
        sakRepository.save(sak)

        logger.info("Demo fnr.: ${fnr.asString}")
    }
}