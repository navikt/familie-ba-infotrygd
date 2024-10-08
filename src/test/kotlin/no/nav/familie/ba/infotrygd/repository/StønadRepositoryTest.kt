package no.nav.familie.ba.infotrygd.repository

import io.mockk.mockk
import no.nav.familie.ba.infotrygd.service.BarnetrygdService
import no.nav.familie.ba.infotrygd.testutil.TestData
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@DataJpaTest
@ActiveProfiles("test")
class StønadRepositoryTest {

    @Autowired
    lateinit var stønadRepository: StønadRepository
    @Autowired
    lateinit var sakRepository: SakRepository

    @Autowired
    lateinit var personRepository: PersonRepository

    @Autowired
    lateinit var utbetalingRepository: UtbetalingRepository

    lateinit var barnetrygdService: BarnetrygdService

    @BeforeEach
    fun setUp() {
        stønadRepository.deleteAll()
        barnetrygdService = BarnetrygdService(
            stønadRepository,
            mockk(),
            sakRepository,
            mockk(),
            utbetalingRepository,
            mockk(),
            mockk(),
            mockk(),
        )
    }

    @Test
    fun `sjekk at antall personer med barnetrygd til pensjon er riktig innenfor hvert av årene 2021, 2022 og 2023`() {
        stønadRepository.saveAll(listOf(
            TestData.stønad(TestData.person(), opphørtFom = "122021", status = "01"), // ordinær barnetrygd opphørt 2021
            TestData.stønad(TestData.person(), opphørtFom = "122022", status = "02"), // utvidet opphørt 2022
            TestData.stønad(TestData.person(), status = "02") // løpende utvidet
        )).also { stønader ->
            utbetalingRepository.saveAll(stønader.map { TestData.utbetaling(it) })
        }
        barnetrygdService.finnPersonerBarnetrygdPensjon("2021").also {
            assertThat(it).hasSize(3)
        }
        barnetrygdService.finnPersonerBarnetrygdPensjon("2022").also {
            assertThat(it).hasSize(2)
        }
        barnetrygdService.finnPersonerBarnetrygdPensjon("2023").also {
            assertThat(it).hasSize(1)
        }
    }

    @Test
    fun `sjekk at man filterer bort utvidede stønader hvor tomMåned er før fomMåned, for disse er feilregistrerte stønader som ikke skal med i uttrekket`() {
        val person = personRepository.saveAndFlush(TestData.person())
        stønadRepository.saveAll(listOf(
            TestData.stønad(person, virkningFom = (999999-202104).toString(), opphørtFom = "012021", status = "02") // utvidet barnetrygd kun 2020
                .also { utbetalingRepository.saveAndFlush(TestData.utbetaling(it)) }
        ))
        barnetrygdService.finnUtvidetBarnetrygdBisys(person.fnr, YearMonth.of(2021, 3)).also {
            assertThat(it.perioder).hasSize(0)
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
    fun `skal finne stønad basert på personKey, iverksattFom, virkningFom og region`() {
        val stønad = stønadRepository.saveAndFlush(TestData.stønad(TestData.person()))
        stønadRepository.findStønad(stønad.personKey, stønad.iverksattFom, stønad.virkningFom, stønad.region).also {
            assertThat(it).isEqualTo(stønad)
        }
    }
}
