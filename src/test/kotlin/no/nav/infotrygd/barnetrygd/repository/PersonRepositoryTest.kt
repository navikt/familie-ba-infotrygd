package no.nav.infotrygd.barnetrygd.repository

import no.nav.infotrygd.barnetrygd.testutil.TestData
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@DataJpaTest
@ActiveProfiles("test")
class PersonRepositoryTest {

    @Autowired
    private lateinit var repository: PersonRepository

    @Test
    fun findByFnr() {
        val person = TestData.person()
        repository.saveAndFlush(person)

        val treff = repository.findByFnr(person.fnr)
        val nullFunn = repository.findByFnr(TestData.foedselsNr())

        assertThat(treff).isEqualToComparingFieldByFieldRecursively(person)
        assertThat(nullFunn).isNull()
    }

    @Test
    fun findByFnrList() {
        val personList = listOf(TestData.person(), TestData.person(tkNr = "2000"))
        repository.saveAll(personList)

        val toTreff = repository.findByFnrList(personList.map { it.fnr })
        val nullFunn = repository.findByFnrList(listOf(1,2,3).map { TestData.foedselsNr() })

        assertThat(toTreff.size).isEqualTo(2)
        assertThat(nullFunn.size).isEqualTo(0)
    }

    @Test
    fun findStønadByFnr() {
        val person = TestData.person()
        val stønad = TestData.stønad(person)
        val stønad2 = TestData.stønad(person, opphørtFom = "111111")

        val copy = person.copy(stønader = listOf(stønad, stønad2))
        repository.saveAndFlush(copy)

        val result = repository.findStønadByFnr(person.fnr)
        assertThat(result).isNotEmpty()
    }

    @Test
    fun findStønadByFnr_negative() {
        val person = TestData.person()
        val stønad = TestData.stønad(person, opphørtFom = "111111")

        repository.saveAndFlush(person.copy(stønader = listOf()))
        val caseIngenStønad = repository.findStønadByFnr(person.fnr)
        repository.saveAndFlush(person.copy(stønader = listOf(stønad)))
        val caseOpphørtStønad = repository.findStønadByFnr(person.fnr)
        val caseNyPerson = repository.findStønadByFnr(TestData.person().fnr)

        assertThat(caseIngenStønad).isEmpty()
        assertThat(caseOpphørtStønad).isEmpty()
        assertThat(caseNyPerson).isEmpty()
    }

    @Test
    fun findStønadByBarnFnr() {
        val person = TestData.person()
        val barn1 = TestData.barn(person)
        val barn2 = TestData.barn(person)
        val stønad = TestData.stønad(person)
        val stønad2 = TestData.stønad(person, opphørtFom = "111111")

        repository.saveAndFlush(person.copy(stønader = listOf(stønad, stønad2), barn = listOf(barn1, barn2)))
        val stønader = repository.findStønadByBarnFnr(barn2.barnFnr)

        assertThat(stønader).isNotEmpty
    }

}