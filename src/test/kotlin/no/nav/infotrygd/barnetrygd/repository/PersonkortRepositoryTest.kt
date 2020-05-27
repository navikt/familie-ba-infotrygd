package no.nav.infotrygd.barnetrygd.repository

import no.nav.infotrygd.barnetrygd.model.ip.Person
import no.nav.infotrygd.barnetrygd.model.ip.Personkort
import no.nav.infotrygd.barnetrygd.nextId
import no.nav.infotrygd.barnetrygd.testutil.TestData
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import java.time.LocalDate

@RunWith(SpringRunner::class)
@DataJpaTest
@ActiveProfiles("test")
class PersonkortRepositoryTest {

    @Autowired
    private lateinit var repository: PersonkortRepository

    @Test
    fun findById() {
        val personkort = Personkort(
            id = nextId(),
            datoSeq = nextId(),
            kontonummer = 123,
            dato = LocalDate.now(),
            fom = LocalDate.now(),
            tom = LocalDate.now(),
            tekst = "hello world",
            person = Person(
                id = nextId(),
                merkeligPersonKey = nextId().toString(),
                fnr = TestData.foedselsNr()
            )
        )
        repository.saveAndFlush(personkort)

        val res = repository.findById(personkort.id).get()
        assertThat(res).isEqualTo(personkort)
    }

    @Test
    fun findByKontonummerAndFnr_ulik_kontonummer() {
        val fnr = TestData.foedselsNr()
        val kontonummer = 292001L
        val relevant = TestData.personkort(
            fnr = fnr,
            kontonummer = kontonummer)
        val urelevant = TestData.personkort(
            fnr = fnr,
            kontonummer = nextId())
        repository.saveAll(listOf(relevant, urelevant))
        val res = repository.findByKontonummerAndFnr(kontonummer, fnr)
        assertThat(res.toList()).isEqualTo(listOf(relevant))
    }

    @Test
    fun findByKontonummerAndFnr_ulik_fnr() {
        val fnr = TestData.foedselsNr()
        val kontonummer = 292001L
        val relevant = TestData.personkort(
            fnr = fnr,
            kontonummer = kontonummer)
        val urelevant = TestData.personkort(
            fnr = TestData.foedselsNr(),
            kontonummer = kontonummer)
        repository.saveAll(listOf(relevant, urelevant))
        val res = repository.findByKontonummerAndFnr(kontonummer, fnr)
        assertThat(res.toList()).isEqualTo(listOf(relevant))
    }

}