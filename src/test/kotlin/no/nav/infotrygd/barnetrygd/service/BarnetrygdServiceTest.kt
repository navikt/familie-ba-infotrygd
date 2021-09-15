package no.nav.infotrygd.barnetrygd.service

import io.mockk.every
import io.mockk.mockk
import no.nav.infotrygd.barnetrygd.model.dl1.Person
import no.nav.infotrygd.barnetrygd.repository.BarnRepository
import no.nav.infotrygd.barnetrygd.repository.PersonRepository
import no.nav.infotrygd.barnetrygd.repository.SakPersonRepository
import no.nav.infotrygd.barnetrygd.repository.SakRepository
import no.nav.infotrygd.barnetrygd.repository.StatusRepository
import no.nav.infotrygd.barnetrygd.repository.StønadRepository
import no.nav.infotrygd.barnetrygd.repository.UtbetalingRepository
import no.nav.infotrygd.barnetrygd.repository.VedtakRepository
import no.nav.infotrygd.barnetrygd.rest.controller.BarnetrygdController
import no.nav.infotrygd.barnetrygd.testutil.TestData
import org.assertj.core.api.Assertions.assertThat
import org.hibernate.exception.SQLGrammarException
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import java.sql.SQLException
import java.time.LocalDateTime
import java.time.YearMonth

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

    @Autowired
    private lateinit var sakPersonRepository: SakPersonRepository

    @Autowired
    private lateinit var vedtakRepository: VedtakRepository

    @Autowired
    private lateinit var utbetalingRepository: UtbetalingRepository

    @Autowired
    private lateinit var statusRepository: StatusRepository

    private lateinit var barnetrygdService: BarnetrygdService

    @Before
    fun setup() {
        barnetrygdService = BarnetrygdService(
            personRepository,
            stonadRepository,
            barnRepository,
            sakRepository,
            vedtakRepository,
            utbetalingRepository,
            statusRepository
        )
    }

    @Test
    fun `findStønadByPerson gir ett treff på stønad som ikke er opphørt`() {
        val person = TestData.person()
        val person2 = TestData.person()
        val stønad = TestData.stønad(person2)
        val stønad2 = TestData.stønad(person, opphørtFom = "111111")

        personRepository.saveAll(listOf(person, person2))
        stonadRepository.saveAll(listOf(stønad, stønad2))

        val stønadResult = barnetrygdService.findStønadByBrukerFnr(listOf(person.fnr, person2.fnr))

        assertThat(stønadResult).hasSize(1)
        assertThat(stønadResult).first().isEqualToComparingFieldByField(barnetrygdService.hentDelytelseOgKonverterTilDto(stønad))
    }

    @Test
    fun `findStønadByPerson gir tomt resultat når region ikke matcher`() {
        val person = TestData.person()
        val stønad = TestData.stønad(person, opphørtFom = "000000", region = "2")

        personRepository.saveAndFlush(person)
        stonadRepository.saveAndFlush(stønad)

        val stønadResult = barnetrygdService.findStønadByBrukerFnr(listOf(person.fnr))
        assertThat(stønadResult).isEmpty()
    }

    @Test
    fun `findStønadByBarn gir ett treff på stønad som ikke er opphørt`() {
        val person = personRepository.saveAll(listOf(TestData.person(), TestData.person()))
        val barn = barnRepository.saveAll(
            listOf(
                TestData.barn(person[0]),
                TestData.barn(person[1], barnetrygdTom = "111111")
            )
        )

        stonadRepository.saveAll(person.map { TestData.stønad(it) })

        val stønadResult = barnetrygdService.findStønadByBarnFnr(barn.map { it.barnFnr })
        assertThat(stønadResult).hasSize(1)
        val stønadResultBarn2 = barnetrygdService.findStønadByBarnFnr(listOf(barn[1].barnFnr))
        assertThat(stønadResultBarn2).hasSize(0)
    }

    @Test
    fun `tellAntallÅpneSaker skal ikke involvere barnRepository når "barn"-listen er tom`() {
        val barnRepositoryMock = mockk<BarnRepository>()
        every { barnRepositoryMock.findBarnByFnrList(emptyList()) } throws
                SQLGrammarException("ORA-00936: uttrykk mangler", SQLException())
        val barnetrygdService = BarnetrygdService(mockk(), mockk(), barnRepositoryMock, mockk(), mockk(), mockk(), mockk())

        assertThat(barnetrygdService.tellAntallÅpneSaker(emptyList(), emptyList())).isEqualTo(0)
    }


    @Test
    fun `hent utvidet barnetrygd for stønad med status 0, utvidet barnetrygdsak og inputdato med dato nå, som kun henter aktiv stønad`() {
        val person = settOppLøpendeUtvidetBarnetrygd("0")
        leggTilUtgåttUtvidetBarnetrygdSak(person) //2019-05 - 2020-04


        val response = barnetrygdService.finnUtvidetBarnetrygd(person.fnr, YearMonth.now())
        assertThat(response.perioder).hasSize(2)
        assertThat(response.perioder).contains(
            BarnetrygdController.UtvidetBarnetrygdPeriode(
                BarnetrygdController.Stønadstype.UTVIDET,
                YearMonth.of(2019, 5), null, 1054.00,
                false,
            )
        )
        assertThat(response.perioder).contains(
            BarnetrygdController.UtvidetBarnetrygdPeriode(
                BarnetrygdController.Stønadstype.SMÅBARNSTILLEGG,
                YearMonth.of(2020, 5), null, 660.00,
                false,
            )
        )
    }


    @Test
    fun `hent utvidet barnetrygd for stønad med status 0, utvidet barnetrygdsak og inputdato med dato i fortiden, som henter aktiv stønad og gammel stønad hvor perioder slås sammen`() {
        val person = settOppLøpendeUtvidetBarnetrygd("0")
        leggTilUtgåttUtvidetBarnetrygdSak(person)

        val response = barnetrygdService.finnUtvidetBarnetrygd(person.fnr, YearMonth.of(2019, 10))
        assertThat(response.perioder).hasSize(2)
        assertThat(response.perioder).contains(
            BarnetrygdController.UtvidetBarnetrygdPeriode(
                BarnetrygdController.Stønadstype.UTVIDET,
                YearMonth.of(2019, 5), null, 1054.00,
                false,
            )
        )
        assertThat(response.perioder).contains(
            BarnetrygdController.UtvidetBarnetrygdPeriode(
                BarnetrygdController.Stønadstype.SMÅBARNSTILLEGG,
                YearMonth.of(2020, 5), null, 660.00,
                false,
            )
        )
    }

    @Test
    fun `finn utvidet barnetrygd for stønad med status 2 skal bruke hardkodede beløp 970 (før mars 2019) og 1054`() {
        val person = personRepository.saveAndFlush(TestData.person())
        leggTilUtgåttUtvidetBarnetrygdSak(person, stønadStatus = "2", beløp = 42.00, iverksattFom = "798097") // februar 2019
        leggTilUtgåttUtvidetBarnetrygdSak(person, stønadStatus = "2", beløp = 42.00, iverksattFom = "798096") // mars 2019

        val response = barnetrygdService.finnUtvidetBarnetrygd(person.fnr, YearMonth.of(2019, 1))

        assertThat(response.perioder).hasSize(2)
        assertThat(response.perioder).contains(
            BarnetrygdController.UtvidetBarnetrygdPeriode(
                BarnetrygdController.Stønadstype.UTVIDET,
                YearMonth.of(2019, 2), YearMonth.of(2020, 4), 970.00,
                false,
            )
        )
        assertThat(response.perioder).contains(
            BarnetrygdController.UtvidetBarnetrygdPeriode(
                BarnetrygdController.Stønadstype.UTVIDET,
                YearMonth.of(2019, 3), YearMonth.of(2020, 4), 1054.00,
                false,
            )
        )
    }

    @Test
    fun `hent utvidet barnetrygd for stønad med status 0, utvidet barnetrygdsak og inputdato med dato i fortiden, som henter aktiv stønad og gammel stønad hvor perioder IKKE slås sammen pga ikke sammenhengende perioder`() {
        val person = settOppLøpendeUtvidetBarnetrygd("0")

        val opphørtStønad = stonadRepository.save(
            TestData.stønad(
                person,
                status = "0",
                opphørtFom = "032020",
                iverksattFom = "798094",
                virkningFom = "798094"
            )
        )
        sakRepository.save(TestData.sak(person, opphørtStønad.saksblokk, opphørtStønad.sakNr, valg = "UT", undervalg = "MB"))
        sakPersonRepository.saveAndFlush(TestData.sakPerson(person))

        utbetalingRepository.save(TestData.utbetaling(opphørtStønad))


        val response = barnetrygdService.finnUtvidetBarnetrygd(person.fnr, YearMonth.of(2019, 10))
        assertThat(response.perioder).hasSize(3)
        assertThat(response.perioder).contains(
            BarnetrygdController.UtvidetBarnetrygdPeriode(
                BarnetrygdController.Stønadstype.UTVIDET,
                YearMonth.of(2020, 5), null, 1054.00,
                false,
            )
        )
        assertThat(response.perioder).contains(
            BarnetrygdController.UtvidetBarnetrygdPeriode(
                BarnetrygdController.Stønadstype.UTVIDET,
                YearMonth.of(2019, 5), YearMonth.of(2020, 3), 1054.00,
                false,
            )
        )
        assertThat(response.perioder).contains(
            BarnetrygdController.UtvidetBarnetrygdPeriode(
                BarnetrygdController.Stønadstype.SMÅBARNSTILLEGG,
                YearMonth.of(2020, 5), null, 660.00,
                false,
            )
        )
    }

    @Test
    fun `hent utvidet barnetrygd for stønad med status 0, utvidet barnetrygdsak og inputdato med dato i fortiden, som henter aktiv stønad og gammel stønad og hvor perioder IKKE slås sammen pga ulike beløp`() {
        val person = settOppLøpendeUtvidetBarnetrygd("0")
        leggTilUtgåttUtvidetBarnetrygdSak(person, 1000.00)

        val response = barnetrygdService.finnUtvidetBarnetrygd(person.fnr, YearMonth.of(2019, 10))
        assertThat(response.perioder).hasSize(3)
        assertThat(response.perioder).contains(
            BarnetrygdController.UtvidetBarnetrygdPeriode(
                BarnetrygdController.Stønadstype.UTVIDET,
                YearMonth.of(2020, 5), null, 1054.00,
                false,
            )
        )

        assertThat(response.perioder).contains(
            BarnetrygdController.UtvidetBarnetrygdPeriode(
                BarnetrygdController.Stønadstype.UTVIDET,
                YearMonth.of(2019, 5), YearMonth.of(2020, 4), 1000.00,
                true,
            )
        )

        assertThat(response.perioder).contains(
            BarnetrygdController.UtvidetBarnetrygdPeriode(
                BarnetrygdController.Stønadstype.SMÅBARNSTILLEGG,
                YearMonth.of(2020, 5), null, 660.00,
                false,
            )
        )
    }

    @Test
    fun `hent utvidet barnetrygd skal korrigere beløp for manuell behandling med delt bosted, 2 barn og barnetrygd inkludert i utbetaling`() {

        val person = personRepository.save(TestData.person())
        val løpendeStønad = stonadRepository.save(TestData.stønad(person, status = "00", opphørtFom = "000000"))
            .also {
                barnRepository.saveAll(
                    listOf(
                        TestData.barn(person, it.iverksattFom, it.virkningFom),
                        TestData.barn(person, it.iverksattFom, it.virkningFom, barnetrygdTom = "111111")
                    )
                )
            }

        sakRepository.save(TestData.sak(person, løpendeStønad.saksblokk, løpendeStønad.sakNr, valg = "UT", undervalg = "MD"))
        sakPersonRepository.saveAndFlush(TestData.sakPerson(person))
        utbetalingRepository.saveAll(
            listOf(
                TestData.utbetaling(løpendeStønad, beløp = 1581.0), // utvidet på aktiv stønad
            )
        )

        val response = barnetrygdService.finnUtvidetBarnetrygd(person.fnr, YearMonth.of(2019, 10))

        assertThat(response.perioder).hasSize(1)
        assertThat(response.perioder).contains(
            BarnetrygdController.UtvidetBarnetrygdPeriode(
                BarnetrygdController.Stønadstype.UTVIDET,
                YearMonth.of(2020, 5), null, 527.00,
                false,
            )
        )
    }

    @Test
    fun `hent utvidet barnetrygd skal korrigere beløp for manuell behandling med delt bosted, 2 barn og barnetrygd inkludert i utbetaling og gammel takst`() {

        val person = personRepository.save(TestData.person())
        val løpendeStønad = stonadRepository.save(TestData.stønad(person, status = "00", opphørtFom = "000000"))
            .also {
                barnRepository.saveAll(
                    listOf(
                        TestData.barn(person, it.iverksattFom, it.virkningFom),
                        TestData.barn(person, it.iverksattFom, it.virkningFom, barnetrygdTom = "111111")
                    )
                )
            }

        sakRepository.save(TestData.sak(person, løpendeStønad.saksblokk, løpendeStønad.sakNr, valg = "UT", undervalg = "MD"))
        sakPersonRepository.saveAndFlush(TestData.sakPerson(person))
        utbetalingRepository.saveAll(
            listOf(
                TestData.utbetaling(løpendeStønad, beløp = 1455.0), // utvidet på aktiv stønad
            )
        )

        val response = barnetrygdService.finnUtvidetBarnetrygd(person.fnr, YearMonth.of(2019, 10))

        assertThat(response.perioder).hasSize(1)
        assertThat(response.perioder).contains(
            BarnetrygdController.UtvidetBarnetrygdPeriode(
                BarnetrygdController.Stønadstype.UTVIDET,
                YearMonth.of(2020, 5), null, 485.00,
                false,
            )
        )
    }

    @Test
    fun `hent utvidet barnetrygd skal korrigere beløp for manuell behandling med delt bosted, barn under 6, barn over 6`() {

        val person = personRepository.save(TestData.person())
        val løpendeStønad = stonadRepository.save(TestData.stønad(person, status = "00", opphørtFom = "000000"))
            .also {
                barnRepository.saveAll(
                    listOf(
                        TestData.barn(person, it.iverksattFom, it.virkningFom),
                        TestData.barn(person, it.iverksattFom, it.virkningFom, barnetrygdTom = "111111")
                    )
                )
            }

        sakRepository.save(TestData.sak(person, løpendeStønad.saksblokk, løpendeStønad.sakNr, valg = "UT", undervalg = "MD"))
        sakPersonRepository.saveAndFlush(TestData.sakPerson(person))

        utbetalingRepository.saveAll(
            listOf(
                TestData.utbetaling(løpendeStønad, beløp = 1731.0), // utvidet på aktiv stønad
            )
        )

        val response = barnetrygdService.finnUtvidetBarnetrygd(person.fnr, YearMonth.of(2019, 10))

        assertThat(response.perioder).hasSize(1)
        assertThat(response.perioder).contains(
            BarnetrygdController.UtvidetBarnetrygdPeriode(
                BarnetrygdController.Stønadstype.UTVIDET,
                YearMonth.of(2020, 5), null, 527.00,
                false,
            )
        )
    }


    @Test
    fun `hent utvidet barnetrygd skal korrigere beløp for manuell behandling med delt bosted, pluss 1 barn over 6`() {

        val person = personRepository.save(TestData.person())
        val løpendeStønad = stonadRepository.save(TestData.stønad(person, status = "00", opphørtFom = "000000"))
            .also {
                barnRepository.saveAll(
                    listOf(
                        TestData.barn(person, it.iverksattFom, it.virkningFom)
                    )
                )
            }

        sakRepository.save(TestData.sak(person, løpendeStønad.saksblokk, løpendeStønad.sakNr, valg = "UT", undervalg = "MD"))
        sakPersonRepository.saveAndFlush(TestData.sakPerson(person))
        utbetalingRepository.saveAll(
            listOf(
                TestData.utbetaling(løpendeStønad, beløp = 1054.0), // utvidet på aktiv stønad
            )
        )

        val response = barnetrygdService.finnUtvidetBarnetrygd(person.fnr, YearMonth.of(2019, 10))

        assertThat(response.perioder).hasSize(1)
        assertThat(response.perioder).contains(
            BarnetrygdController.UtvidetBarnetrygdPeriode(
                BarnetrygdController.Stønadstype.UTVIDET,
                YearMonth.of(2020, 5), null, 527.00,
                false,
            )
        )
    }

    @Test
    fun `hent utvidet barnetrygd skal bruke beløp for manuell behandling med delt bosted, pluss 1 barn over 6 når beløpet er orginalt riktig`() {

        val person = personRepository.save(TestData.person())
        val løpendeStønad = stonadRepository.save(TestData.stønad(person, status = "00", opphørtFom = "000000"))
            .also {
                barnRepository.saveAll(
                    listOf(
                        TestData.barn(person, it.iverksattFom, it.virkningFom)
                    )
                )
            }

        sakRepository.save(TestData.sak(person, løpendeStønad.saksblokk, løpendeStønad.sakNr, valg = "UT", undervalg = "MD"))
        sakPersonRepository.saveAndFlush(TestData.sakPerson(person))
        utbetalingRepository.saveAll(
            listOf(
                TestData.utbetaling(løpendeStønad, beløp = 527.0), // utvidet på aktiv stønad
            )
        )

        val response = barnetrygdService.finnUtvidetBarnetrygd(person.fnr, YearMonth.of(2019, 10))

        assertThat(response.perioder).hasSize(1)
        assertThat(response.perioder).contains(
            BarnetrygdController.UtvidetBarnetrygdPeriode(
                BarnetrygdController.Stønadstype.UTVIDET,
                YearMonth.of(2020, 5), null, 527.00,
                false,
            )
        )
    }

    @Test
    fun `skal hente utvidet barnetrygd stønader en person for et bestemt år`() {
        val person = personRepository.saveAndFlush(TestData.person())
        val person2 = personRepository.saveAndFlush(TestData.person())
        stonadRepository.saveAll(listOf(
            TestData.stønad(person, virkningFom = (999999-201901).toString(), opphørtFom = "112019", status = "02"), // utvidet barnetrygd 2019
            TestData.stønad(person, virkningFom = (999999-202001).toString(), status = "02"), // utvidet barnetrygd fra 2020
            TestData.stønad(person2, virkningFom = (999999-201901).toString(), opphørtFom = "112019", status = "02"), // utvidet barnetrygd 2019
            TestData.stønad(person2, virkningFom = (999999-202001).toString(), status = "02"), // utvidet barnetrygd fra 2020
        ))

        barnetrygdService.finnPerioderMedUtvidetBarnetrygdForÅr(person.fnr, 2019).also {
            assertThat(it.perioder).hasSize(1)
            assertThat(it.perioder[0].fomMåned).isEqualTo(YearMonth.of(2019, 1))
            assertThat(it.perioder[0].tomMåned).isEqualTo(YearMonth.of(2019, 11))
            assertThat(it.perioder[0].maxDelingsprosent).isEqualTo("100")
            assertThat(it.perioder[0].sisteVedtakPaaIdent).isEqualTo(LocalDateTime.of(2020, 5, 1, 0, 0))
        }

        barnetrygdService.finnPerioderMedUtvidetBarnetrygdForÅr(person.fnr, 2020).also {
            assertThat(it.perioder).hasSize(1)
            assertThat(it.perioder[0].fomMåned).isEqualTo(YearMonth.of(2020, 1))
            assertThat(it.perioder[0].tomMåned).isEqualTo(null)
            assertThat(it.perioder[0].maxDelingsprosent).isEqualTo("100")
            assertThat(it.perioder[0].sisteVedtakPaaIdent).isEqualTo(LocalDateTime.of(2020, 5, 1, 0, 0))
        }

        barnetrygdService.finnPerioderMedUtvidetBarnetrygdForÅr(person.fnr, 2021).also {
            assertThat(it.perioder).hasSize(1)
            assertThat(it.perioder[0].fomMåned).isEqualTo(YearMonth.of(2020, 1))
            assertThat(it.perioder[0].tomMåned).isEqualTo(null)
            assertThat(it.perioder[0].maxDelingsprosent).isEqualTo("100")
            assertThat(it.perioder[0].sisteVedtakPaaIdent).isEqualTo(LocalDateTime.of(2020, 5, 1, 0, 0))
        }
    }

    private fun settOppLøpendeUtvidetBarnetrygd(stønadStatus: String): Person {
        val person = personRepository.save(TestData.person())
        val løpendeStønad = stonadRepository.save(TestData.stønad(person, status = stønadStatus, opphørtFom = "000000"))
        sakRepository.save(TestData.sak(person, løpendeStønad.saksblokk, løpendeStønad.sakNr, valg = "UT", undervalg = "MB"))
        utbetalingRepository.saveAll(
            listOf(
                TestData.utbetaling(løpendeStønad, kontonummer = "06040000", beløp = 660.00), //småbarnstillegg aktiv stønad
                TestData.utbetaling(løpendeStønad), // utvidet på aktiv stønad
            )
        )
        return person

    }

    private fun leggTilUtgåttUtvidetBarnetrygdSak(person: Person,
                                                  beløp: Double? = null,
                                                  stønadStatus: String = "0",
                                                  iverksattFom: String = (999999 - 201905).toString(),
                                                  virkningFom: String = iverksattFom) {
        val opphørtStønad = stonadRepository.save(
            TestData.stønad(
                person,
                status = stønadStatus,
                opphørtFom = "042020",
                iverksattFom = iverksattFom,
                virkningFom = virkningFom
            )
        )
        sakRepository.save(TestData.sak(person, opphørtStønad.saksblokk, opphørtStønad.sakNr, valg = "UT", undervalg = "MB"))
        sakPersonRepository.saveAndFlush(TestData.sakPerson(person))
        if (beløp == null){
            utbetalingRepository.save(TestData.utbetaling(opphørtStønad))
        } else {
            utbetalingRepository.save(TestData.utbetaling(opphørtStønad, beløp = beløp))
        }

    }
}
