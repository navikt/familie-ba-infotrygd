package no.nav.infotrygd.barnetrygd.service

import no.nav.infotrygd.barnetrygd.repository.BarnRepository
import no.nav.infotrygd.barnetrygd.repository.PersonRepository
import no.nav.infotrygd.barnetrygd.repository.SakRepository
import no.nav.infotrygd.barnetrygd.repository.StønadRepository
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
    fun `finnes skal returnere true`() {
        val soeker = TestData.person()
        val barn = TestData.barn(soeker)

        personRepository.saveAndFlush(soeker)
        barnRepository.saveAndFlush(barn)

        val soekerResult = barnetrygdService.finnes(listOf(soeker.fnr), null)
        val barnResult = barnetrygdService.finnes(listOf(), listOf(barn.barnFnr))

        assertThat(soekerResult).isTrue()
        assertThat(barnResult).isTrue()
    }

    @Test
    fun `finnes skal returnere false`() {
        val soekerFnr = TestData.foedselsNr()
        val barnFnr = TestData.foedselsNr()

        val resultEmptyEmpty = barnetrygdService.finnes(listOf(soekerFnr), listOf(barnFnr))
        val resultEmptyNull = barnetrygdService.finnes(listOf(soekerFnr), null)

        assertThat(resultEmptyEmpty).isFalse()
        assertThat(resultEmptyNull).isFalse()
    }

    @Test
    fun `mottar barnetrygd skal returnere true når en av personene har en løpende sak`() {
        val person = TestData.person()
        val person2 = TestData.person()
        val stønad = TestData.stønad(person2)
        val stønad2 = TestData.stønad(person, opphørtFom = "111111")

        personRepository.saveAll(listOf(person, person2))
        stonadRepository.saveAll(listOf(stønad, stønad2))

        val mottarBarnetrygd = barnetrygdService.mottarBarnetrygd(listOf(person.fnr, person2.fnr), null)

        assertThat(mottarBarnetrygd).isTrue()
    }

    @Test
    fun `mottarBarnetrygd skal returnere false for opphørt stønad`() {
        val person = TestData.person()
        val person2 = TestData.person()
        val stønad = TestData.stønad(person, opphørtFom = "111111")
        val stønad2 = TestData.stønad(person2, opphørtFom = "111111")
        val barn = TestData.barn(person2)

        personRepository.saveAll(listOf(person, person2))
        stonadRepository.saveAll(listOf(stønad, stønad2))
        barnRepository.saveAndFlush(barn)

        val case1 = barnetrygdService.mottarBarnetrygd(listOf(person.fnr), null)
        val case2 = barnetrygdService.mottarBarnetrygd(listOf(), listOf(barn.barnFnr))
        assertThat(case1).isFalse()
        assertThat(case2).isFalse()
    }

    @Test
    fun `mottarBarnetrygd skal returnere false når region ikke matcher`() {
        val person = TestData.person()
        val stønad = TestData.stønad(person, opphørtFom = "000000", region = "A")

        personRepository.saveAndFlush(person)
        stonadRepository.saveAndFlush(stønad)

        val mottarBarnetrygd = barnetrygdService.mottarBarnetrygd(listOf(person.fnr), null)
        assertThat(mottarBarnetrygd).isFalse()
    }

    @Test
    fun `mottarBarnetrygd skal returnere true hvis det finnes løpende sak tilknyttet barnet`() {
        val person = TestData.person()
        val person2 = TestData.person()
        val stønad = TestData.stønad(person2, opphørtFom = "000000")
        val barn = TestData.barn(person2)


        personRepository.saveAll(listOf(person, person2))
        stonadRepository.saveAndFlush(stønad)
        barnRepository.saveAndFlush(barn)

        val mottarBarnetrygd = barnetrygdService.mottarBarnetrygd(listOf(person.fnr), listOf(barn.barnFnr))
        assertThat(mottarBarnetrygd).isTrue()
    }

    @Test
    fun `mottarBarnetrygd skal returnere false når det finnes sak tilknyttet barnet som er opphørt`() {
        val person = TestData.person()
        val person2 = TestData.person()
        val stønad = TestData.stønad(person2, opphørtFom = "000000")
        val barn = TestData.barn(person2, barnetrygdTom = "111111")


        personRepository.saveAll(listOf(person, person2))
        stonadRepository.saveAndFlush(stønad)
        barnRepository.saveAndFlush(barn)

        val mottarBarnetrygd = barnetrygdService.mottarBarnetrygd(listOf(person.fnr), listOf(barn.barnFnr))
        assertThat(mottarBarnetrygd).isFalse()
    }
}