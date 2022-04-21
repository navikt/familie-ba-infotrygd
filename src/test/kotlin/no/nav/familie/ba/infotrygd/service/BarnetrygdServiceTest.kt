package no.nav.familie.ba.infotrygd.service

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.ba.infotrygd.model.dl1.Person
import no.nav.familie.ba.infotrygd.repository.BarnRepository
import no.nav.familie.ba.infotrygd.repository.HendelseRepository
import no.nav.familie.ba.infotrygd.repository.PersonRepository
import no.nav.familie.ba.infotrygd.repository.SakPersonRepository
import no.nav.familie.ba.infotrygd.repository.SakRepository
import no.nav.familie.ba.infotrygd.repository.StatusRepository
import no.nav.familie.ba.infotrygd.repository.StønadRepository
import no.nav.familie.ba.infotrygd.repository.UtbetalingRepository
import no.nav.familie.ba.infotrygd.repository.VedtakRepository
import no.nav.familie.ba.infotrygd.rest.controller.BisysController
import no.nav.familie.ba.infotrygd.testutil.TestData
import no.nav.familie.ba.infotrygd.testutil.TestData.foedselsNr
import no.nav.familie.eksterne.kontrakter.skatteetaten.SkatteetatenPeriode
import org.assertj.core.api.Assertions.assertThat
import org.hibernate.exception.SQLGrammarException
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.core.env.Environment
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import java.sql.SQLException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter

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

    @Autowired
    private lateinit var hendelseRepository: HendelseRepository

    private val environment: Environment = mockk(relaxed = true)

    private lateinit var barnetrygdService: BarnetrygdService

    @Before
    fun setup() {
        barnetrygdService = BarnetrygdService(
            stonadRepository,
            barnRepository,
            sakRepository,
            vedtakRepository,
            utbetalingRepository,
            statusRepository,
            environment,
            hendelseRepository
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
        val barnetrygdService = BarnetrygdService(mockk(), barnRepositoryMock, mockk(), mockk(), mockk(), mockk(), mockk(), mockk())

        assertThat(barnetrygdService.tellAntallÅpneSaker(emptyList(), emptyList())).isEqualTo(0)
    }


    @Test
    fun `hent utvidet barnetrygd for stønad med status 0, utvidet barnetrygdsak og inputdato med dato nå, som kun henter aktiv stønad, manuelt beregnet`() {
        val person = settOppLøpendeUtvidetBarnetrygd(MANUELT_BEREGNET_STATUS)
        leggTilUtgåttUtvidetBarnetrygdSak(person) //2019-05 - 2020-04


        val response = barnetrygdService.finnUtvidetBarnetrygdBisys(person.fnr, YearMonth.now())
        assertThat(response.perioder).hasSize(2)
        assertThat(response.perioder).contains(
            BisysController.UtvidetBarnetrygdPeriode(
                BisysController.Stønadstype.UTVIDET,
                YearMonth.of(2019, 5), null, 1054.00,
                true,
                deltBosted = false
            )
        )
        assertThat(response.perioder).contains(
            BisysController.UtvidetBarnetrygdPeriode(
                BisysController.Stønadstype.SMÅBARNSTILLEGG,
                YearMonth.of(2020, 5), null, 660.00,
                false,
                deltBosted = false
            )
        )
    }


    @Test
    fun `hent utvidet barnetrygd for stønad med status 0, utvidet barnetrygdsak og inputdato med dato i fortiden, som henter aktiv stønad og gammel stønad hvor perioder slås sammen`() {
        val person = settOppLøpendeUtvidetBarnetrygd(MANUELT_BEREGNET_STATUS)
        leggTilUtgåttUtvidetBarnetrygdSak(person)

        val response = barnetrygdService.finnUtvidetBarnetrygdBisys(person.fnr, YearMonth.of(2019, 10))
        assertThat(response.perioder).hasSize(2)
        assertThat(response.perioder).contains(
            BisysController.UtvidetBarnetrygdPeriode(
                BisysController.Stønadstype.UTVIDET,
                YearMonth.of(2019, 5), null, 1054.00,
                true,
                false
            )
        )
        assertThat(response.perioder).contains(
            BisysController.UtvidetBarnetrygdPeriode(
                BisysController.Stønadstype.SMÅBARNSTILLEGG,
                YearMonth.of(2020, 5), null, 660.00,
                false,
                false
            )
        )
    }

    @Test
    fun `finn utvidet barnetrygd for stønad med status 2 skal bruke hardkodede beløp 970 (før mars 2019) og 1054`() {
        val person = personRepository.saveAndFlush(TestData.person())
        leggTilUtgåttUtvidetBarnetrygdSak(person, stønadStatus = "2", beløp = 42.00, iverksattFom = "798097") // februar 2019
        leggTilUtgåttUtvidetBarnetrygdSak(person, stønadStatus = "2", beløp = 42.00, iverksattFom = "798096") // mars 2019

        val response = barnetrygdService.finnUtvidetBarnetrygdBisys(person.fnr, YearMonth.of(2019, 1))

        assertThat(response.perioder).hasSize(2)
        assertThat(response.perioder).contains(
            BisysController.UtvidetBarnetrygdPeriode(
                BisysController.Stønadstype.UTVIDET,
                YearMonth.of(2019, 2), YearMonth.of(2020, 4), 970.00,
                false,
                false
            )
        )
        assertThat(response.perioder).contains(
            BisysController.UtvidetBarnetrygdPeriode(
                BisysController.Stønadstype.UTVIDET,
                YearMonth.of(2019, 3), YearMonth.of(2020, 4), 1054.00,
                false,
                false
            )
        )
    }

    @Test
    fun `hent utvidet barnetrygd for stønad med status 0, utvidet barnetrygdsak og inputdato med dato i fortiden, som henter aktiv stønad og gammel stønad hvor perioder IKKE slås sammen pga ikke sammenhengende perioder`() {
        val person = settOppLøpendeUtvidetBarnetrygd(MANUELT_BEREGNET_STATUS)

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


        val response = barnetrygdService.finnUtvidetBarnetrygdBisys(person.fnr, YearMonth.of(2019, 10))
        assertThat(response.perioder).hasSize(3)
        assertThat(response.perioder).contains(
            BisysController.UtvidetBarnetrygdPeriode(
                BisysController.Stønadstype.UTVIDET,
                YearMonth.of(2020, 5), null, 1054.00,
                true,
                false
            )
        )
        assertThat(response.perioder).contains(
            BisysController.UtvidetBarnetrygdPeriode(
                BisysController.Stønadstype.UTVIDET,
                YearMonth.of(2019, 5), YearMonth.of(2020, 3), 1054.00,
                true,
                false
            )
        )
        assertThat(response.perioder).contains(
            BisysController.UtvidetBarnetrygdPeriode(
                BisysController.Stønadstype.SMÅBARNSTILLEGG,
                YearMonth.of(2020, 5), null, 660.00,
                false,
                false
            )
        )
    }

    @Test
    fun `hent utvidet barnetrygd for stønad med status 0, utvidet barnetrygdsak og inputdato med dato i fortiden, som henter aktiv stønad og gammel stønad og hvor perioder IKKE slås sammen pga ulike beløp`() {
        val person = settOppLøpendeUtvidetBarnetrygd(MANUELT_BEREGNET_STATUS)
        leggTilUtgåttUtvidetBarnetrygdSak(person, 1000.00)

        val response = barnetrygdService.finnUtvidetBarnetrygdBisys(person.fnr, YearMonth.of(2019, 10))
        assertThat(response.perioder).hasSize(3)
        assertThat(response.perioder).contains(
            BisysController.UtvidetBarnetrygdPeriode(
                BisysController.Stønadstype.UTVIDET,
                YearMonth.of(2020, 5), null, 1054.00,
                true,
                false
            )
        )

        assertThat(response.perioder).contains(
            BisysController.UtvidetBarnetrygdPeriode(
                BisysController.Stønadstype.UTVIDET,
                YearMonth.of(2019, 5), YearMonth.of(2020, 4), 1000.00,
                true,
                false
            )
        )

        assertThat(response.perioder).contains(
            BisysController.UtvidetBarnetrygdPeriode(
                BisysController.Stønadstype.SMÅBARNSTILLEGG,
                YearMonth.of(2020, 5), null, 660.00,
                false,
                false
            )
        )
    }

    @Test
    fun `hent utvidet barnetrygd skal returnere manuell behandling med delt bosted, 2 barn og manuelt beregnet beløp`() {

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

        val response = barnetrygdService.finnUtvidetBarnetrygdBisys(person.fnr, YearMonth.of(2019, 10))

        assertThat(response.perioder).hasSize(1)
        assertThat(response.perioder).contains(
            BisysController.UtvidetBarnetrygdPeriode(
                BisysController.Stønadstype.UTVIDET,
                YearMonth.of(2020, 5), null, 1581.00,
                true,
                true
            )
        )
    }

    @Test
    fun `Skal returnere SkatteetatenPerioderResponse med perioder av utvidet barnetrygd stønader en person for et bestemt år`() {
        val person = personRepository.saveAndFlush(TestData.person())
        val person2 = personRepository.saveAndFlush(TestData.person())
        val sakDeltBosted = sakRepository.saveAndFlush(TestData.sak(person = person, undervalg = "MD", valg = "UT"))
        val sakIkkeDeltBosted = sakRepository.saveAndFlush(TestData.sak(person = person, undervalg = "EF", valg = "UT",saksnummer = "02"))
        val sakManuletBeregnet = sakRepository.saveAndFlush(TestData.sak(person = person, undervalg = "ME", valg = "UT", saksnummer = "03"))

        stonadRepository.saveAll(listOf(
            // utvidet barnetrygd 2019 med delt bosted
            TestData.stønad(person, virkningFom = (999999-201901).toString(), opphørtFom = "112019", status = "02", saksblokk = sakDeltBosted.saksblokk, saksnummer = sakDeltBosted.saksnummer, region = sakDeltBosted.region),
            // utvidet barnetrygd fra 2020 hvor saken er ikke delt bosted
            TestData.stønad(person, virkningFom = (999999-202001).toString(), status = "02", saksblokk = sakIkkeDeltBosted.saksblokk, saksnummer = sakIkkeDeltBosted.saksnummer, region = sakIkkeDeltBosted.region),
            // utvidet barnetrygd fra 2017 hvor saken er manuelt beregnet og vi dermed ikke kan utlede delt bosted. Denne slår også sammen perioden fra stønaden 2017-2018
            TestData.stønad(person, virkningFom = (999999-201701).toString(), opphørtFom = "062017", status = "02", saksblokk = sakManuletBeregnet.saksblokk, saksnummer = sakManuletBeregnet.saksnummer, region = sakManuletBeregnet.region),
            TestData.stønad(person, virkningFom = (999999-201706).toString(), opphørtFom = "042018", status = "02", saksblokk = sakManuletBeregnet.saksblokk, saksnummer = sakManuletBeregnet.saksnummer, region = sakManuletBeregnet.region),

            //Dette er testdata fra en annen person og skal ikke bli med i uttrekket
            TestData.stønad(person2, virkningFom = (999999-201901).toString(), opphørtFom = "112019", status = "02"), // utvidet barnetrygd 2019
            TestData.stønad(person2, virkningFom = (999999-202001).toString(), status = "02"), // utvidet barnetrygd fra 2020
        )).also { stønader ->
            utbetalingRepository.saveAll(stønader.map { TestData.utbetaling(it) })
        }

        //Denne verifiserer at stønaden er deltbosted
        barnetrygdService.finnPerioderUtvidetBarnetrygdSkatt(person.fnr.asString, 2019).also {
            assertThat(it.brukere).hasSize(1)
            assertThat(it.brukere.first().perioder).hasSize(1)
            assertThat(it.brukere.first().perioder.first().fraMaaned).isEqualTo("2019-01")
            assertThat(it.brukere.first().perioder.first().tomMaaned).isEqualTo("2019-10")
            assertThat(it.brukere.first().perioder.first().
            delingsprosent).isEqualTo(SkatteetatenPeriode.Delingsprosent._50)
            assertThat(it.brukere.first().sisteVedtakPaaIdent).isEqualTo(LocalDateTime.of(2020, 5, 1, 0, 0))
        }

        //Denne verifiserer at stønaden er ikke deltbosted
        barnetrygdService.finnPerioderUtvidetBarnetrygdSkatt(person.fnr.asString, 2020).also {
            assertThat(it.brukere).hasSize(1)
            assertThat(it.brukere.first().perioder).hasSize(1)
            assertThat(it.brukere.first().perioder.first().fraMaaned).isEqualTo("2020-01")
            assertThat(it.brukere.first().perioder.first().tomMaaned).isNull()
            assertThat(it.brukere.first().perioder.first().delingsprosent).isEqualTo(SkatteetatenPeriode.Delingsprosent._0)
            assertThat(it.brukere.first().sisteVedtakPaaIdent).isEqualTo(LocalDateTime.of(2020, 5, 1, 0, 0))
        }
        //Denne verifiserer samme stønad som over, bare at stønaden er løpende og input er året etter
        barnetrygdService.finnPerioderUtvidetBarnetrygdSkatt(person.fnr.asString, 2021).also {
            assertThat(it.brukere).hasSize(1)
            assertThat(it.brukere.first().perioder).hasSize(1)
            assertThat(it.brukere.first().perioder.first().fraMaaned).isEqualTo("2020-01")
            assertThat(it.brukere.first().perioder.first().tomMaaned).isNull()
            assertThat(it.brukere.first().perioder.first().delingsprosent).isEqualTo(SkatteetatenPeriode.Delingsprosent._0)
            assertThat(it.brukere.first().sisteVedtakPaaIdent).isEqualTo(LocalDateTime.of(2020, 5, 1, 0, 0))
        }

        //Denne verifiserer at stønaden er manuelt  beregnet og vi dermed ikke kan utlede delt bosted
        barnetrygdService.finnPerioderUtvidetBarnetrygdSkatt(person.fnr.asString, 2017).also {
            assertThat(it.brukere).hasSize(1)
            assertThat(it.brukere.first().perioder).hasSize(1)
            assertThat(it.brukere.first().perioder.first().fraMaaned).isEqualTo("2017-01")
            assertThat(it.brukere.first().perioder.first().tomMaaned).isEqualTo("2018-03")
            assertThat(it.brukere.first().perioder.first().delingsprosent).isEqualTo(SkatteetatenPeriode.Delingsprosent.usikker)
        }
    }

    @Test
    fun `Skal returnere tom SkatteetatenPerioderResponse hvor en person ikke har noen perioder med utvidet barnetrygd stønader`() {
        val person = personRepository.saveAndFlush(TestData.person())
        val sakDeltBosted = sakRepository.saveAndFlush(TestData.sak(person = person, undervalg = "MD", valg = "UT"))

        stonadRepository.saveAll(listOf(
            // utvidet barnetrygd stønad som er feilregistrert fordi opphørtFom == virkningFom
            TestData.stønad(person, virkningFom = (999999-201911).toString(), opphørtFom = "112019", status = "02", saksblokk = sakDeltBosted.saksblokk, saksnummer = sakDeltBosted.saksnummer, region = sakDeltBosted.region),
        ))


        barnetrygdService.finnPerioderUtvidetBarnetrygdSkatt(person.fnr.asString, 2019).also {
            assertThat(it.brukere).hasSize(0)
        }
    }

    @Test
    fun `Skal slå sammen overlappende måneder`() {
        val person = personRepository.saveAndFlush(TestData.person())
        val sakDeltBosted = sakRepository.saveAndFlush(TestData.sak(person = person, undervalg = "MD", valg = "UT"))

        stonadRepository.saveAll(listOf(
            // utvidet barnetrygd stønad som er feilregistrert fordi opphørtFom == virkningFom
            TestData.stønad(person, virkningFom = (999999-202108).toString(), opphørtFom = "092021", status = "02", saksblokk = sakDeltBosted.saksblokk, saksnummer = sakDeltBosted.saksnummer, region = sakDeltBosted.region),
            TestData.stønad(person, virkningFom = (999999-202108).toString(), opphørtFom = "122021", status = "02", saksblokk = sakDeltBosted.saksblokk, saksnummer = sakDeltBosted.saksnummer, region = sakDeltBosted.region),
            TestData.stønad(person, virkningFom = (999999-202201).toString(), opphørtFom = "000000", status = "02", saksblokk = sakDeltBosted.saksblokk, saksnummer = sakDeltBosted.saksnummer, region = sakDeltBosted.region),
        )).also { stønader ->
            utbetalingRepository.saveAll(stønader.map { TestData.utbetaling(it) })
        }

        barnetrygdService.finnPerioderUtvidetBarnetrygdSkatt(person.fnr.asString, 2021).also {
            assertThat(it.brukere).hasSize(1)
        }

        barnetrygdService.finnUtvidetBarnetrygdBisys(person.fnr, YearMonth.of(2021, 1)).also {
            assertThat(it.perioder).hasSize(1)
        }
    }


    @Test
    fun `skal hente personer klar for migrering`() {
        val person = personRepository.saveAndFlush(TestData.person())
        val personSomFiltreresVekkPgaAntallBarnIStønadStørreEnnMaksAntallBarn =
            personRepository.saveAndFlush(TestData.person())
        val personSomFiltreresVekkPgaBarnMedSpesiellStønadstype =
            personRepository.saveAndFlush(TestData.person())
        val stønad1 = TestData.stønad(person, virkningFom = (999999 - 202001).toString(), status = "01", antallBarn = 1)

        val stønad2 = TestData.stønad(
            personSomFiltreresVekkPgaAntallBarnIStønadStørreEnnMaksAntallBarn,
            virkningFom = (999999 - 202001).toString(),
            status = "02",
            antallBarn = 2
        )
        val stønad3 = TestData.stønad(
            personSomFiltreresVekkPgaBarnMedSpesiellStønadstype,
            virkningFom = (999999 - 202001).toString(),
            status = "01",
            antallBarn = 1
        )

        stonadRepository.saveAll(listOf(stønad1, stønad2, stønad3)).also {
            sakRepository.saveAll(it.map { TestData.sak(it, valg = "OR", undervalg = "OS") })
        }
        val barn1 = TestData.barn(stønad1)
        barnRepository.saveAll(listOf(barn1,
                                      TestData.barn(stønad2),
                                      TestData.barn(stønad3, stønadstype = "N")))

        barnetrygdService.finnPersonerKlarForMigrering(0, 10, "OR", "OS")
            .also {
                assertThat(it.first).hasSize(1).contains(person.fnr.asString) //Det finnes ingen saker på personene
            }
    }

    @Test
    fun `skal filtrere bort person med løpende stønad på barn over 18 år ved migreirng`() {
        val personMedStønadPåBarnOver18år = personRepository.saveAndFlush(TestData.person())

        personRepository.saveAndFlush(TestData.person())
        val stønad1 = TestData.stønad(personMedStønadPåBarnOver18år, virkningFom = (999999 - 202001).toString(), status = "01", antallBarn = 1)


        stonadRepository.saveAll(listOf(stønad1)).also {
            sakRepository.saveAll(it.map { TestData.sak(it, valg = "OR", undervalg = "OS") })
        }

        barnRepository.saveAll(listOf(TestData.barn(stønad = stønad1, barnFnr = foedselsNr(foedselsdato = LocalDate.now().minusMonths(18L*12 + 1)))))

        barnetrygdService.finnPersonerKlarForMigrering(0, 10, "OR", "OS")
            .also {
                assertThat(it.first).hasSize(0)
            }
    }

    @Test
    fun `skal filtrere på tknr ved migreirng i preprod`() {
        every { environment.activeProfiles } returns listOf("preprod").toTypedArray() andThen listOf("prod").toTypedArray()

        val person = personRepository.saveAndFlush(TestData.person(tkNr = "0312"))
        val personSomFiltreresVekkPgaTknrIPreprod =
            personRepository.saveAndFlush(TestData.person())
        val stønad1 = TestData.stønad(person, virkningFom = (999999 - 202001).toString(), status = "01", antallBarn = 1)
        val stønad3 = TestData.stønad(
            personSomFiltreresVekkPgaTknrIPreprod, virkningFom = (999999 - 202001).toString(), status = "02", antallBarn = 1
        )

        stonadRepository.saveAll(listOf(stønad1, stønad3)).also {
            sakRepository.saveAll(it.map { TestData.sak(it, valg = "OR", undervalg = "OS") })
        }

        barnRepository.saveAll(listOf(TestData.barn(stønad1),
                                      TestData.barn(stønad3)))

        val personerKlareForMigreringIPreprod = barnetrygdService.finnPersonerKlarForMigrering(0, 10, "OR", "OS")
        val personerKlareForMigreringIProd = barnetrygdService.finnPersonerKlarForMigrering(0, 10, "OR", "OS")

        assertThat(personerKlareForMigreringIPreprod.first).hasSize(1).contains(person.fnr.asString)
        assertThat(personerKlareForMigreringIProd.first).hasSize(2)
    }

    @Test
    fun `harSendtBrevForrigeMåned skal returnere tom liste hvis det ikke er sendt ut noen brev med brevkode B002 siste måned`() {
        val person = personRepository.saveAndFlush(TestData.person(tkNr = "0312"))
        hendelseRepository.saveAll(listOf(
            TestData.hendelse(person, 79779884, "B001"), //2022-01-15
        ))

        assertThat(barnetrygdService.harSendtBrevForrigeMåned(listOf(person.fnr), listOf("B002"))).hasSize(0)
    }

    @Test
    fun `harSendtBrevForrigeMåned skal returnere tom liste hvis det er sendt ut brev med kode B001 for lengre enn en måned siden`() {
        val person = personRepository.saveAndFlush(TestData.person(tkNr = "0312"))
        hendelseRepository.saveAll(listOf(
            TestData.hendelse(person, 79788868, "B001"), //2021-11-31
        ))

        assertThat(barnetrygdService.harSendtBrevForrigeMåned(listOf(person.fnr), listOf("B001"))).hasSize(0)
    }

    @Test
    fun `harSendtBrevForrigeMåned skal returnere liste med hendleser hvis det er sendt ut brev med kode B001 nylig`() {
        val person = personRepository.saveAndFlush(TestData.person(tkNr = "0312"))
        hendelseRepository.saveAll(listOf(
            TestData.hendelse(person, 99999999 - LocalDateTime.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyyMMdd")).toLong(), "B001"),
        ))

        assertThat(barnetrygdService.harSendtBrevForrigeMåned(listOf(person.fnr), listOf("B001"))).hasSize(1)
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

    companion object {
        const val MANUELT_BEREGNET_STATUS = "0"
        const val UTVIDET_BARNETRYGD_STATUS = "2"
    }
}
