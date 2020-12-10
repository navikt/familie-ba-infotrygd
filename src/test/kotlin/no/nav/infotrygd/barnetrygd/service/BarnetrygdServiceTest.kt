package no.nav.infotrygd.barnetrygd.service

import no.nav.infotrygd.barnetrygd.repository.BarnRepository
import no.nav.infotrygd.barnetrygd.repository.PersonRepository
import no.nav.infotrygd.barnetrygd.repository.SakRepository
import no.nav.infotrygd.barnetrygd.repository.StønadRepository
import no.nav.infotrygd.barnetrygd.rest.api.toStønadDto
import no.nav.infotrygd.barnetrygd.testutil.TestData
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@DataJpaTest
@ActiveProfiles("test")
internal class BarnetrygdServiceTest {

    @Autowired
    private lateinit var personRepository: PersonRepository

    @Autowired
    private lateinit var stonadRepository: StønadRepository

    @Autowired
    private lateinit var barnRepository: BarnRepository

    @Autowired
    private lateinit var sakRepository: SakRepository

    private lateinit var barnetrygdService: BarnetrygdService

    @Before
    fun setup() {
        barnetrygdService = BarnetrygdService(personRepository, stonadRepository, barnRepository, sakRepository)
    }

    @Test
    fun `findStønadByPerson gir ett treff på stønad som ikke er opphørt`() {
        val person = TestData.person()
        val person2 = TestData.person()
        val stønad = TestData.stønad(person2)
        val stønad2 = TestData.stønad(person, opphørtFom = "111111")

        personRepository.saveAll(listOf(person, person2))
        stonadRepository.saveAll(listOf(stønad, stønad2))

        val stænadResult = barnetrygdService.findStønadByPerson(listOf(person.fnr, person2.fnr))

        assertThat(stænadResult).hasSize(1)
        assertThat(stænadResult).first().isEqualToComparingFieldByField(stønad.toStønadDto())
    }

    @Test
    fun `findStønadByPerson gir tomt resultat når region ikke matcher`() {
        val person = TestData.person()
        val stønad = TestData.stønad(person, opphørtFom = "000000", region = "2")

        personRepository.saveAndFlush(person)
        stonadRepository.saveAndFlush(stønad)

        val stønadResult = barnetrygdService.findStønadByPerson(listOf(person.fnr))
        assertThat(stønadResult).isEmpty()
    }

    @Test
    fun `findStønadByBarn gir ett treff på stønad som ikke er opphørt`() {
        val person = personRepository.saveAll(listOf(TestData.person(), TestData.person()))
        val barn = barnRepository.saveAll(listOf(TestData.barn(person[0]),
                                                 TestData.barn(person[1], barnetrygdTom = "111111")))

        stonadRepository.saveAll(person.map { TestData.stønad(it) })

        val stønadResult = barnetrygdService.findStønadByBarn(barn.map { it.barnFnr })
        assertThat(stønadResult).hasSize(1)
        val stønadResultBarn2 = barnetrygdService.findStønadByBarn(listOf(barn[1].barnFnr))
        assertThat(stønadResultBarn2).hasSize(0)
    }
}