package no.nav.familie.ba.infotrygd.repository

import io.mockk.mockk
import no.nav.familie.ba.infotrygd.service.BarnetrygdService
import no.nav.familie.ba.infotrygd.testutil.TestData
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.domain.Pageable
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@RunWith(SpringRunner::class)
@DataJpaTest
@ActiveProfiles("test")
class StønadRepositoryTest {

    @Autowired
    lateinit var stønadRepository: StønadRepository
    @Autowired
    lateinit var sakRepository: SakRepository

    @Autowired
    lateinit var personRepository: PersonRepository

    lateinit var barnetrygdService: BarnetrygdService

    @Before
    fun setUp() {
        stønadRepository.deleteAll()
        barnetrygdService = BarnetrygdService(stønadRepository, mockk(), mockk(), mockk(), mockk(), mockk(), mockk(), mockk())
    }

    @Test
    fun `sjekk at antall personer med utvidet barnetrygd er riktig innenfor hvert av årene 2019, 2020 og 2021`() {
        val personFraInneværendeÅr = TestData.person()
        stønadRepository.saveAll(listOf(
            TestData.stønad(TestData.person(), virkningFom = (999999-201901).toString(), status = "01"), // ordinær barnetrygd fra 2019
            TestData.stønad(personFraInneværendeÅr, status = "02"), // utvidet barnetrygd fra 2020
            TestData.stønad(TestData.person(), opphørtFom = "122020", status = "02") // utvidet barnetrygd kun 2020
        ))
        barnetrygdService.finnPersonerMedUtvidetBarnetrygd("2019").also {
            assertThat(it).hasSize(0)
        }
        barnetrygdService.finnPersonerMedUtvidetBarnetrygd("2020").also {
            assertThat(it).hasSize(2)
        }
        barnetrygdService.finnPersonerMedUtvidetBarnetrygd("2021").also {
            assertThat(it).hasSize(1).extracting("ident").contains(personFraInneværendeÅr.fnr.asString)
        }
    }


    @Test
    fun `sjekk at man filterer bort utvidede stønader hvor fomMåned = tomMåned, for disse er feilregistrerte stønader som ikke skal med i uttrekket`() {
        stønadRepository.saveAll(listOf(
            TestData.stønad(TestData.person(), virkningFom = (999999-202101).toString(), opphørtFom = "012021", status = "02") // utvidet barnetrygd kun 2020
        ))
        barnetrygdService.finnPersonerMedUtvidetBarnetrygd("2021").also {
            assertThat(it).hasSize(0)
        }
    }

    @Test
    fun `sjekk at man filterer bort utvidede stønader hvor tomMåned er før fomMåned, for disse er feilregistrerte stønader som ikke skal med i uttrekket`() {
        stønadRepository.saveAll(listOf(
            TestData.stønad(TestData.person(), virkningFom = (999999-202104).toString(), opphørtFom = "012021", status = "02") // utvidet barnetrygd kun 2020
        ))
        barnetrygdService.finnPersonerMedUtvidetBarnetrygd("2021").also {
            assertThat(it).hasSize(0)
        }
    }

    @Test
    fun findSenesteIverksattFomByPersonKey() {
        val tidligsteIverksattFom = 201701
        val senesteIverksattFom = 201905
        val person = TestData.person()
        stønadRepository.saveAll(listOf(
            TestData.stønad(person, iverksattFom = (999999-tidligsteIverksattFom).toString()),
            TestData.stønad(person, iverksattFom = (999999-201902).toString()),
            TestData.stønad(person, iverksattFom = (999999-senesteIverksattFom).toString())
        ))
        barnetrygdService.finnSisteVedtakPåPerson(person.personKey).also {
            assertThat(it)
                .isEqualTo(YearMonth.parse("$senesteIverksattFom", DateTimeFormatter.ofPattern("yyyyMM")))
        }
    }

    @Test
    fun `skal hente utvidet barnetrygd stønader en person for et bestemt år`() {
        val person = personRepository.saveAndFlush(TestData.person())
        val person2 = personRepository.saveAndFlush(TestData.person())
        stønadRepository.saveAll(listOf(
            TestData.stønad(person, virkningFom = (999999-201901).toString(), opphørtFom = "112019", status = "02"), // utvidet barnetrygd 2019
            TestData.stønad(person, virkningFom = (999999-202001).toString(), status = "02"), // utvidet barnetrygd fra 2020
            TestData.stønad(person2, virkningFom = (999999-201901).toString(), opphørtFom = "112019", status = "02"), // utvidet barnetrygd 2019
            TestData.stønad(person2, virkningFom = (999999-202001).toString(), status = "02"), // utvidet barnetrygd fra 2020
        ))
        stønadRepository.findStønadByÅrAndStatusKoderAndFnr(person.fnr, 2019,  "02").also {
            assertThat(it).hasSize(1).extracting("fnr").contains(person.fnr)
        }
        stønadRepository.findStønadByÅrAndStatusKoderAndFnr( person.fnr, 2020, "02").also {
            assertThat(it).hasSize(1).extracting("fnr").contains(person.fnr)
        }
        stønadRepository.findStønadByÅrAndStatusKoderAndFnr(person.fnr,2021, "02").also {
            assertThat(it).hasSize(1).extracting("fnr").contains(person.fnr)
        }
    }

    @Test
    fun `skal søke opp personer som har en sakstype fra input`() {
        val person = personRepository.saveAndFlush(TestData.person())
        val person2 = personRepository.saveAndFlush(TestData.person())
        stønadRepository.saveAll(listOf(
            TestData.stønad(person, virkningFom = (999999-201901).toString(), opphørtFom = "112019", status = "01"),
            TestData.stønad(person, virkningFom = (999999-202001).toString(), status = "01"), // løpende barnetrygd
            TestData.stønad(person2, virkningFom = (999999-201901).toString(), opphørtFom = "112019", status = "01"), // utvidet barnetrygd 2019
            TestData.stønad(person2, virkningFom = (999999-202001).toString(), status = "02"), // løpende barnetrygd
        )).also { sakRepository.saveAll(it.map { TestData.sak(it, valg = "OR", undervalg = "OS") }) }

        stønadRepository.findKlarForMigrering(Pageable.unpaged(), "OR", "OS").toSet().also {
            assertThat(it).hasSize(2).extracting("fnr").contains(person.fnr, person2.fnr) //Det finnes ingen saker på personene
        }

        stønadRepository.findKlarForMigrering(Pageable.unpaged(), "UT", "EF").toSet().also {
            assertThat(it).hasSize(0)
        }
    }

    @Test
    fun `skal finne stønad basert på personKey, iverksattFom, virkningFom og region`() {
        val stønad = stønadRepository.saveAndFlush(TestData.stønad(TestData.person()))
        stønadRepository.findStønad(stønad.personKey, stønad.iverksattFom, stønad.virkningFom, stønad.region).also {
            assertThat(it).isEqualTo(stønad)
        }
    }
}
