package no.nav.infotrygd.barnetrygd.testutil

import no.nav.infotrygd.barnetrygd.repository.BarnRepository
import no.nav.infotrygd.barnetrygd.repository.PersonRepository
import no.nav.infotrygd.barnetrygd.repository.SakRepository
import no.nav.infotrygd.barnetrygd.repository.StønadRepository
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
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @PostConstruct
    fun init() {
        val person = TestData.person()
        val barn = TestData.barn(person)
        val sak = TestData.sak(person).let { it.copy(stønadList = listOf(TestData.stønad(person, it))) }

        personRepository.saveAndFlush(person)
        barnRepository.saveAndFlush(barn)
        stønadRepository.saveAndFlush(TestData.stønad(mottaker = person, opphørtFom = LocalDate.now()
            .format(DateTimeFormatter.ofPattern("ddMMyy"))))
        sakRepository.saveAndFlush(sak)

        logger.info("Demo fnr.: ${person.fnr.asString}\nDemo barnFnr.: ${barn.barnFnr.asString}")
    }
}