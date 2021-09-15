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
    lateinit var sakPersonRepository: SakPersonRepository

    @Autowired
    lateinit var stønadRepository: StønadRepository

    @Autowired
    lateinit var personRepository: PersonRepository

    @Autowired
    lateinit var barnRepository: BarnRepository

    @Before
    fun setUp() {
        sakRepository.deleteAll()
        personRepository.deleteAll()
        barnRepository.deleteAll()
    }

    @Test
    fun findBarnetrygdsakerByFnr() {
        val person = personRepository.saveAndFlush(TestData.person())
        val stønad = stønadRepository.saveAndFlush(TestData.stønad(person))
        val sak = sakRepository.saveAndFlush(TestData.sak(person, stønad.saksblokk, stønad.sakNr))
            .also { sakPersonRepository.saveAndFlush(TestData.sakPerson(person)) }

        sakRepository.findBarnetrygdsakerByFnr(person.fnr).also {
            assertThat(it).extracting("personKey").first().isEqualTo(person.personKey)
            assertThat(it).usingRecursiveFieldByFieldElementComparator().isEqualTo(listOf(sak))
        }
    }

    @Test
    fun `findBarnetrygdsakerByFnr med person uten barnetrygdsak`() {
        val person = personRepository.saveAndFlush(TestData.person())
        sakRepository.saveAndFlush(TestData.sak(person).copy(kapittelNr = "FA"))

        val sak = sakRepository.findBarnetrygdsakerByFnr(person.fnr)

        assertThat(sak).hasSize(0)
    }

    @Test
    fun findBarnetrygdsakerByBarnFnr() {
        val person = personRepository.saveAndFlush(TestData.person())
        val sak = sakRepository.saveAndFlush(TestData.sak(person))
        val barn = barnRepository.saveAndFlush(TestData.barn(person))

        val result = sakRepository.findBarnetrygdsakerByBarnFnr(listOf(barn.barnFnr))

        assertThat(result).hasSize(1)
        assertThat(result).extracting("personKey").first()
            .isEqualTo(person.personKey)
        assertThat(result)
            .usingRecursiveFieldByFieldElementComparator()
            .isEqualTo(listOf(sak))
    }

    @Test
    fun `findBarnetrygdsakerByBarnFnr query med tom liste og barn som ikke finnes gir tomt resultat`() {
        val person = personRepository.saveAndFlush(TestData.person())
        sakRepository.saveAndFlush(TestData.sak(person))

        listOf(listOf(person.fnr), listOf()).forEach {
            sakRepository.findBarnetrygdsakerByBarnFnr(it).apply {
                assertThat(this).hasSize(0)
            }
        }
    }
}
