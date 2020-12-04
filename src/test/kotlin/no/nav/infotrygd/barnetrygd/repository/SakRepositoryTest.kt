package no.nav.infotrygd.barnetrygd.repository
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
class SakRepositoryTest {

    @Autowired
    lateinit var sakRepository: SakRepository

    @Autowired
    lateinit var personRepository: PersonRepository

    @Before
    fun setUp() {
        sakRepository.deleteAll()
        personRepository.deleteAll()
    }

    @Test
    fun findSakerPåPersonByFnr() {
        val person = TestData.person()
        personRepository.saveAndFlush(person)
        sakRepository.saveAndFlush(TestData.sak(person))

        val sak = sakRepository.findSakerPåPersonByFnr(person.fnr)

        assertThat(sak).hasSize(1)
        assertThat(sak[0].s01Personkey).isEqualTo(person.personKey)
    }
}