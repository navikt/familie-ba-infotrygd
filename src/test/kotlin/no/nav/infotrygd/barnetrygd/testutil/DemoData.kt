package no.nav.infotrygd.barnetrygd.testutil

import no.nav.infotrygd.barnetrygd.repository.PersonRepository
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
@Profile("demoData")
class DemoData(
    private val personRepository: PersonRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @PostConstruct
    fun init() {
        val person = TestData.person()
        val barn = TestData.barn(person)

        personRepository.saveAndFlush(person.copy(barn= listOf(barn)))

        logger.info("Demo fnr.: ${person.fnr.asString}\nDemo barnFnr.: ${barn.barnFnr.asString}")
    }
}