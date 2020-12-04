package no.nav.infotrygd.barnetrygd.testutil

import no.nav.infotrygd.barnetrygd.repository.BarnRepository
import no.nav.infotrygd.barnetrygd.repository.PersonRepository
import no.nav.infotrygd.barnetrygd.repository.SakRepository
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
@Profile("demoData")
class DemoData(
    private val personRepository: PersonRepository,
    private val barnRepository: BarnRepository,
    private val sakRepository: SakRepository,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @PostConstruct
    fun init() {
        val person = TestData.person()
        val barn = TestData.barn(person)
        val sak = TestData.sak(person)

        personRepository.saveAndFlush(person)
        barnRepository.saveAndFlush(barn)
        sakRepository.saveAll(listOf(sak, TestData.sak(person)))

        logger.info("Demo fnr.: ${person.fnr.asString}\nDemo barnFnr.: ${barn.barnFnr.asString}")
    }
}