package no.nav.infotrygd.barnetrygd.testutil

import no.nav.infotrygd.barnetrygd.model.db2.Beslutning
import no.nav.infotrygd.barnetrygd.model.db2.LøpeNrFnr
import no.nav.infotrygd.barnetrygd.model.db2.StønadDb2
import no.nav.infotrygd.barnetrygd.repository.*
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.annotation.PostConstruct

@Component
@Profile("demoData")
class DemoData(
    private val personRepository: PersonRepository,
    private val barnRepository: BarnRepository,
    private val stønadRepository: StønadRepository,
    private val sakRepository: SakRepository,
    private val vedtakRepository: VedtakRepository,
    private val løpeNrFnrRepository: LøpeNrFnrRepository,
    private val beslutningRepository: BeslutningRepository,
    private val stønadDb2Repository: StønadDb2Repository,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @PostConstruct
    fun init() {
        val person = TestData.person()
        val sak = TestData.sak(person).let { it.copy(stønadList = listOf(TestData.stønad(person, it))) }
        val barn = sak.stønadList[0].barn.first()

        personRepository.saveAndFlush(person).also {
            løpeNrFnrRepository.saveAndFlush(LøpeNrFnr(1, it.fnr.asString))
        }
        barnRepository.saveAndFlush(barn)
        sakRepository.saveAndFlush(sak)
        vedtakRepository.saveAndFlush(TestData.vedtak(sak)).also {
            stønadDb2Repository.saveAndFlush(StønadDb2(1, "BA", 1))
            beslutningRepository.saveAndFlush(Beslutning(1, it.vedtakId, "J"))
        }
        stønadRepository.saveAndFlush(TestData.stønad(mottaker = person, opphørtFom = LocalDate.now()
            .format(DateTimeFormatter.ofPattern("ddMMyy"))))

        logger.info("Demo fnr.: ${person.fnr.asString}\nDemo barnFnr.: ${barn.barnFnr.asString}")
    }
}