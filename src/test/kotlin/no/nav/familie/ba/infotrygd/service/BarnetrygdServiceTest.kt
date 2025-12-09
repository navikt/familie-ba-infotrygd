package no.nav.familie.ba.infotrygd.service

import io.mockk.every
import io.mockk.mockk
import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.familie.ba.infotrygd.model.dl1.Person
import no.nav.familie.ba.infotrygd.model.dl1.tilTrunkertStønad
import no.nav.familie.ba.infotrygd.repository.BarnRepository
import no.nav.familie.ba.infotrygd.repository.HendelseRepository
import no.nav.familie.ba.infotrygd.repository.LøpeNrFnrRepository
import no.nav.familie.ba.infotrygd.repository.PersonRepository
import no.nav.familie.ba.infotrygd.repository.SakRepository
import no.nav.familie.ba.infotrygd.repository.StatusRepository
import no.nav.familie.ba.infotrygd.repository.StønadRepository
import no.nav.familie.ba.infotrygd.repository.StønadsklasseRepository
import no.nav.familie.ba.infotrygd.repository.UtbetalingRepository
import no.nav.familie.ba.infotrygd.repository.VedtakRepository
import no.nav.familie.ba.infotrygd.rest.controller.BisysController
import no.nav.familie.ba.infotrygd.rest.controller.PensjonController
import no.nav.familie.ba.infotrygd.rest.controller.PensjonController.YtelseProsent
import no.nav.familie.ba.infotrygd.rest.controller.PensjonController.YtelseTypeEkstern
import no.nav.familie.ba.infotrygd.testutil.TestData
import org.assertj.core.api.Assertions.assertThat
import org.hibernate.exception.SQLGrammarException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import java.sql.SQLException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter

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
    private lateinit var vedtakRepository: VedtakRepository

    @Autowired
    private lateinit var utbetalingRepository: UtbetalingRepository

    @Autowired
    private lateinit var statusRepository: StatusRepository

    @Autowired
    private lateinit var hendelseRepository: HendelseRepository

    @Autowired
    private lateinit var stønadsklasseRepository: StønadsklasseRepository

    @Autowired
    private lateinit var løpeNrFnrRepository: LøpeNrFnrRepository

    private lateinit var barnetrygdService: BarnetrygdService

    @BeforeEach
    fun setup() {
        barnetrygdService =
            BarnetrygdService(
                stonadRepository,
                barnRepository,
                sakRepository,
                vedtakRepository,
                utbetalingRepository,
                statusRepository,
                hendelseRepository,
                personRepository,
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
        val barn =
            barnRepository.saveAll(
                listOf(
                    TestData.barn(person[0]),
                    TestData.barn(person[1], barnetrygdTom = "111111"),
                ),
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
        val barnetrygdService =
            BarnetrygdService(
                mockk(),
                barnRepositoryMock,
                mockk(),
                mockk(),
                mockk(),
                mockk(),
                mockk(),
                mockk(),
            )

        assertThat(barnetrygdService.tellAntallÅpneSaker(emptyList(), emptyList())).isEqualTo(0)
    }

    @Test
    fun `finn barnetrygd for pensjon - finner full ordniær barnetrygd`() {
        val person = settOppLøpendeOrdinærBarnetrygd(ORDINÆR_BARNETRYGD_STATUS)

        val response = barnetrygdService.finnBarnetrygdForPensjon(person.fnr, YearMonth.now()).single()
        assertThat(response.barnetrygdPerioder).contains(
            PensjonController.BarnetrygdPeriode(
                personIdent =
                    barnRepository
                        .findBarnByPersonkey(person.personKey)
                        .single()
                        .barnFnr.asString,
                delingsprosentYtelse = YtelseProsent.FULL,
                ytelseTypeEkstern = YtelseTypeEkstern.ORDINÆR_BARNETRYGD,
                stønadFom = YearMonth.of(2020, 5),
                stønadTom = YearMonth.from(LocalDate.MAX),
                kildesystem = "Infotrygd",
                utbetaltPerMnd = 1054,
                sakstypeEkstern = PensjonController.SakstypeEkstern.NASJONAL,
                iverksatt = YearMonth.of(2020, 5),
            ),
        )
    }

    @Test
    fun `finn barnetrygd for pensjon - finner full ordniær barnetrygd for alle barna`() {
        val person = settOppLøpendeOrdinærBarnetrygd(ORDINÆR_BARNETRYGD_STATUS, antallBarn = 3)
        val barna = barnRepository.findBarnByPersonkey(person.personKey)

        val response = barnetrygdService.finnBarnetrygdForPensjon(person.fnr, YearMonth.now()).single()

        assertThat(barna.size).isEqualTo(3)
        assertThat(response.barnetrygdPerioder).containsAll(
            barna.map {
                PensjonController.BarnetrygdPeriode(
                    personIdent = it.barnFnr.asString,
                    delingsprosentYtelse = YtelseProsent.FULL,
                    ytelseTypeEkstern = YtelseTypeEkstern.ORDINÆR_BARNETRYGD,
                    stønadFom = YearMonth.of(2020, 5),
                    stønadTom = YearMonth.from(LocalDate.MAX),
                    kildesystem = "Infotrygd",
                    utbetaltPerMnd = 1054,
                    sakstypeEkstern = PensjonController.SakstypeEkstern.NASJONAL,
                    iverksatt = YearMonth.of(2020, 5),
                )
            },
        )
    }

    @Test
    fun `finn barnetrygd for pensjon - skal finne barnetrygd fra relatert sak`() {
        val person = settOppLøpendeOrdinærBarnetrygd(ORDINÆR_BARNETRYGD_STATUS, antallBarn = 2)
        val barna = barnRepository.findBarnByPersonkey(person.personKey)
        val relatertPerson = personRepository.saveAndFlush(TestData.person())
        val relatertSakOpphørtFom = YearMonth.now()

        leggTilUtgåttUtvidetBarnetrygdSak(
            relatertPerson,
            opphørtFom = relatertSakOpphørtFom.format(DateTimeFormatter.ofPattern("MMyyyy")),
            barnFnr = barna.first().barnFnr,
        )

        val response = barnetrygdService.finnBarnetrygdForPensjon(person.fnr, YearMonth.now().minusMonths(1))
        val barnetrygdFraRelatertSak = response.find { it.fnr == relatertPerson.fnr.asString }

        assertThat(response.size).isEqualTo(2)
        assertThat(barnetrygdFraRelatertSak!!.barnetrygdPerioder).containsOnly(
            PensjonController.BarnetrygdPeriode(
                personIdent = barna.first().barnFnr.asString,
                delingsprosentYtelse = YtelseProsent.USIKKER,
                ytelseTypeEkstern = YtelseTypeEkstern.UTVIDET_BARNETRYGD,
                stønadFom = YearMonth.of(2019, 5),
                stønadTom = relatertSakOpphørtFom.minusMonths(1),
                kildesystem = "Infotrygd",
                utbetaltPerMnd = SATS_UTVIDET.toInt(),
                sakstypeEkstern = PensjonController.SakstypeEkstern.NASJONAL,
                iverksatt = YearMonth.of(2019, 5),
            ),
        )
    }

    @Test
    fun `finn barnetrygd for pensjon - ignorerer småbarnstillegg og finner løpende utvidet fra og med dato gitt av foregående periode`() {
        val person = settOppLøpendeUtvidetBarnetrygd(MANUELT_BEREGNET_STATUS)
        leggTilUtgåttUtvidetBarnetrygdSak(
            person,
            opphørtFom = YearMonth.now().format(DateTimeFormatter.ofPattern("MMyyyy")),
            barnFnr = barnRepository.findBarnByPersonkey(person.personKey).single().barnFnr,
        )

        val response = barnetrygdService.finnBarnetrygdForPensjon(person.fnr, YearMonth.now()).single()
        assertThat(response.barnetrygdPerioder).containsOnly(
            PensjonController.BarnetrygdPeriode(
                personIdent =
                    barnRepository
                        .findBarnByPersonkey(person.personKey)
                        .single()
                        .barnFnr.asString,
                delingsprosentYtelse = YtelseProsent.USIKKER,
                ytelseTypeEkstern = YtelseTypeEkstern.UTVIDET_BARNETRYGD,
                stønadFom = YearMonth.of(2019, 5),
                stønadTom = YearMonth.from(LocalDate.MAX),
                kildesystem = "Infotrygd",
                utbetaltPerMnd = 1054,
                sakstypeEkstern = PensjonController.SakstypeEkstern.NASJONAL,
                iverksatt = YearMonth.of(2019, 5),
            ),
        )
    }

    @Test
    fun `finn barnetrygd for pensjon - håndterer overlapp (kan skje ved revurdering tilbake i tid) ved å forkorte forrige tom-dato`() {
        val person = settOppLøpendeUtvidetBarnetrygd() // default stønad fom 2020-05
        leggTilUtgåttUtvidetBarnetrygdSak( // default stønad fom 2019-05
            person = person,
            beløp = 2000.0, // setter det forskjellig fra den første stønaden for å hindre at periodene slås sammen.
            opphørtFom = YearMonth.of(2022, 1).format(DateTimeFormatter.ofPattern("MMyyyy")),
            barnFnr = barnRepository.findBarnByPersonkey(person.personKey).single().barnFnr,
        )

        barnetrygdService
            .finnBarnetrygdForPensjon(person.fnr, YearMonth.of(2020, 1))
            .single()
            .barnetrygdPerioder
            .apply {
                val periode1 = find { it.stønadFom == YearMonth.of(2019, 5) }!!
                val periode2 = find { it.stønadFom == YearMonth.of(2020, 5) }!!

                assertThat(periode1.stønadTom).isEqualTo(periode2.stønadFom.minusMonths(1))
            }
    }

    @Test
    fun `finn barnetrygd for pensjon - håndterer fullstendige overlapp (lik fomDato) ved å velge perioden senest iverksatt`() {
        val person = settOppLøpendeUtvidetBarnetrygd() //  stønad fom 2020-05, iverksatt 2020-05
        leggTilUtgåttUtvidetBarnetrygdSak(
            virkningFom = (999999 - 202005).toString(), // stønad fom 2020-05, iverksatt 2019-05
            person = person,
            opphørtFom = YearMonth.of(2022, 1).format(DateTimeFormatter.ofPattern("MMyyyy")),
            barnFnr = barnRepository.findBarnByPersonkey(person.personKey).single().barnFnr,
        )

        val barnetrygdPerioder =
            barnetrygdService.finnBarnetrygdForPensjon(person.fnr, YearMonth.of(2020, 1)).single().barnetrygdPerioder

        assertThat(barnetrygdPerioder).hasSize(1)
        assertThat(barnetrygdPerioder.single().iverksatt).isEqualTo(barnetrygdService.finnSisteVedtakPåPerson(person.personKey))
    }

    @Test
    fun `finn barnetrygd for pensjon - finner perioden med stønadTom samme måned som fraDato`() {
        val person = personRepository.save(TestData.person())
        val fraDato = YearMonth.now()
        val stønadTom = fraDato.format(DateTimeFormatter.ofPattern("MMyyyy"))

        val stønad =
            stonadRepository.save(
                TestData.stønad(
                    person,
                    opphørtFom = fraDato.plusMonths(1).format(DateTimeFormatter.ofPattern("MMyyyy")),
                ),
            )
        sakRepository.save(TestData.sak(person, stønad.saksblokk, stønad.sakNr))
        utbetalingRepository.save(TestData.utbetaling(stønad, utbetalingTom = stønadTom))
        barnRepository.save(TestData.barn(stønad))

        val response = barnetrygdService.finnBarnetrygdForPensjon(person.fnr, fraDato).single()

        assertThat(response.barnetrygdPerioder).contains(
            PensjonController.BarnetrygdPeriode(
                personIdent =
                    barnRepository
                        .findBarnByStønad(stønad.tilTrunkertStønad())
                        .single()
                        .barnFnr.asString,
                delingsprosentYtelse = YtelseProsent.FULL,
                ytelseTypeEkstern = YtelseTypeEkstern.ORDINÆR_BARNETRYGD,
                stønadFom = YearMonth.of(2020, 5),
                stønadTom = fraDato,
                kildesystem = "Infotrygd",
                utbetaltPerMnd = 1054,
                sakstypeEkstern = PensjonController.SakstypeEkstern.NASJONAL,
                iverksatt = YearMonth.of(2020, 5),
            ),
        )
    }

    @Test
    fun `hent utvidet barnetrygd for stønad med status 0, utvidet barnetrygdsak og inputdato med dato nå, som kun henter aktiv stønad, manuelt beregnet`() {
        val person = settOppLøpendeUtvidetBarnetrygd(MANUELT_BEREGNET_STATUS)
        leggTilUtgåttUtvidetBarnetrygdSak(person) // 2019-05 - 2020-04

        val response = barnetrygdService.finnUtvidetBarnetrygdBisys(person.fnr, YearMonth.now())
        assertThat(response.perioder).hasSize(2)
        assertThat(response.perioder).contains(
            BisysController.UtvidetBarnetrygdPeriode(
                BisysController.Stønadstype.UTVIDET,
                YearMonth.of(2019, 5),
                null,
                1054.00,
                true,
                deltBosted = false,
            ),
        )
        assertThat(response.perioder).contains(
            BisysController.UtvidetBarnetrygdPeriode(
                BisysController.Stønadstype.SMÅBARNSTILLEGG,
                YearMonth.of(2020, 5),
                null,
                660.00,
                false,
                deltBosted = false,
            ),
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
                YearMonth.of(2019, 5),
                null,
                1054.00,
                true,
                false,
            ),
        )
        assertThat(response.perioder).contains(
            BisysController.UtvidetBarnetrygdPeriode(
                BisysController.Stønadstype.SMÅBARNSTILLEGG,
                YearMonth.of(2020, 5),
                null,
                660.00,
                false,
                false,
            ),
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
                YearMonth.of(2019, 2),
                YearMonth.of(2020, 4),
                970.00,
                false,
                false,
            ),
        )
        assertThat(response.perioder).contains(
            BisysController.UtvidetBarnetrygdPeriode(
                BisysController.Stønadstype.UTVIDET,
                YearMonth.of(2019, 3),
                YearMonth.of(2020, 4),
                1054.00,
                false,
                false,
            ),
        )
    }

    @Test
    fun `hent utvidet barnetrygd for stønad med status 0, utvidet barnetrygdsak og inputdato med dato i fortiden, som henter aktiv stønad og gammel stønad hvor perioder IKKE slås sammen pga ikke sammenhengende perioder`() {
        val person = settOppLøpendeUtvidetBarnetrygd(MANUELT_BEREGNET_STATUS)

        val opphørtStønad =
            stonadRepository.save(
                TestData.stønad(
                    person,
                    status = "0",
                    opphørtFom = "032020",
                    iverksattFom = "798094",
                    virkningFom = "798094",
                ),
            )
        sakRepository.save(TestData.sak(person, opphørtStønad.saksblokk, opphørtStønad.sakNr, valg = "UT", undervalg = "MB"))

        utbetalingRepository.save(TestData.utbetaling(opphørtStønad))

        val response = barnetrygdService.finnUtvidetBarnetrygdBisys(person.fnr, YearMonth.of(2019, 10))
        assertThat(response.perioder).hasSize(3)
        assertThat(response.perioder).contains(
            BisysController.UtvidetBarnetrygdPeriode(
                BisysController.Stønadstype.UTVIDET,
                YearMonth.of(2020, 5),
                null,
                1054.00,
                true,
                false,
            ),
        )
        assertThat(response.perioder).contains(
            BisysController.UtvidetBarnetrygdPeriode(
                BisysController.Stønadstype.UTVIDET,
                YearMonth.of(2019, 5),
                YearMonth.of(2020, 3),
                1054.00,
                true,
                false,
            ),
        )
        assertThat(response.perioder).contains(
            BisysController.UtvidetBarnetrygdPeriode(
                BisysController.Stønadstype.SMÅBARNSTILLEGG,
                YearMonth.of(2020, 5),
                null,
                660.00,
                false,
                false,
            ),
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
                YearMonth.of(2020, 5),
                null,
                1054.00,
                true,
                false,
            ),
        )

        assertThat(response.perioder).contains(
            BisysController.UtvidetBarnetrygdPeriode(
                BisysController.Stønadstype.UTVIDET,
                YearMonth.of(2019, 5),
                YearMonth.of(2020, 4),
                1000.00,
                true,
                false,
            ),
        )

        assertThat(response.perioder).contains(
            BisysController.UtvidetBarnetrygdPeriode(
                BisysController.Stønadstype.SMÅBARNSTILLEGG,
                YearMonth.of(2020, 5),
                null,
                660.00,
                false,
                false,
            ),
        )
    }

    @Test
    fun `hent utvidet barnetrygd skal returnere manuell behandling med delt bosted, 2 barn og manuelt beregnet beløp`() {
        val person = personRepository.save(TestData.person())
        val løpendeStønad =
            stonadRepository
                .save(TestData.stønad(person, status = "00", opphørtFom = "000000"))
                .also {
                    barnRepository.saveAll(
                        listOf(
                            TestData.barn(person, it.iverksattFom, it.virkningFom),
                            TestData.barn(person, it.iverksattFom, it.virkningFom, barnetrygdTom = "111111"),
                        ),
                    )
                }

        sakRepository.save(TestData.sak(person, løpendeStønad.saksblokk, løpendeStønad.sakNr, valg = "UT", undervalg = "MD"))
        utbetalingRepository.saveAll(
            listOf(
                TestData.utbetaling(løpendeStønad, beløp = 1581.0), // utvidet på aktiv stønad
            ),
        )

        val response = barnetrygdService.finnUtvidetBarnetrygdBisys(person.fnr, YearMonth.of(2019, 10))

        assertThat(response.perioder).hasSize(1)
        assertThat(response.perioder).contains(
            BisysController.UtvidetBarnetrygdPeriode(
                BisysController.Stønadstype.UTVIDET,
                YearMonth.of(2020, 5),
                null,
                1581.00,
                true,
                true,
            ),
        )
    }

    @Test
    fun `gyldige beløp`() {
        val gyldigeBeløp2Barn2022 = barnetrygdService.utledListeMedGyldigeUtbetalingsbeløp(2, 2022).toList()
        assertThat(gyldigeBeløp2Barn2022).hasSize(3).containsExactly(1581, 1892, 2203)

        val gyldigeBeløp2Barn2021 = barnetrygdService.utledListeMedGyldigeUtbetalingsbeløp(2, 2021).toList()
        assertThat(gyldigeBeløp2Barn2021).hasSize(4).containsExactly(1581, 1731, 1881, 2181)
    }

    @Test
    fun `Skal slå sammen overlappende måneder`() {
        val person = personRepository.saveAndFlush(TestData.person())
        val sakDeltBosted = sakRepository.saveAndFlush(TestData.sak(person = person, undervalg = "MD", valg = "UT"))

        stonadRepository
            .saveAll(
                listOf(
                    // utvidet barnetrygd stønad som er feilregistrert fordi opphørtFom == virkningFom
                    TestData.stønad(
                        person,
                        virkningFom = (999999 - 202108).toString(),
                        opphørtFom = "092021",
                        status = "02",
                        saksblokk = sakDeltBosted.saksblokk,
                        saksnummer = sakDeltBosted.saksnummer,
                        region = sakDeltBosted.region,
                    ),
                    TestData.stønad(
                        person,
                        virkningFom = (999999 - 202108).toString(),
                        opphørtFom = "122021",
                        status = "02",
                        saksblokk = sakDeltBosted.saksblokk,
                        saksnummer = sakDeltBosted.saksnummer,
                        region = sakDeltBosted.region,
                    ),
                    TestData.stønad(
                        person,
                        virkningFom = (999999 - 202201).toString(),
                        opphørtFom = "000000",
                        status = "02",
                        saksblokk = sakDeltBosted.saksblokk,
                        saksnummer = sakDeltBosted.saksnummer,
                        region = sakDeltBosted.region,
                    ),
                ),
            ).also { stønader ->
                utbetalingRepository.saveAll(stønader.map { TestData.utbetaling(it) })
            }

        barnetrygdService.finnUtvidetBarnetrygdBisys(person.fnr, YearMonth.of(2021, 1)).also {
            assertThat(it.perioder).hasSize(1)
        }
    }

    @Test
    fun `harSendtBrevForrigeMåned skal returnere tom liste hvis det ikke er sendt ut noen brev med brevkode B002 siste måned`() {
        val person = personRepository.saveAndFlush(TestData.person(tkNr = "0312"))
        hendelseRepository.saveAll(
            listOf(
                TestData.hendelse(person, 79779884, "B001"), // 2022-01-15
            ),
        )

        assertThat(barnetrygdService.harSendtBrevForrigeMåned(listOf(person.fnr), listOf("B002"))).hasSize(0)
    }

    @Test
    fun `harSendtBrevForrigeMåned skal returnere tom liste hvis det er sendt ut brev med kode B001 for lengre enn en måned siden`() {
        val person = personRepository.saveAndFlush(TestData.person(tkNr = "0312"))
        hendelseRepository.saveAll(
            listOf(
                TestData.hendelse(person, 79788868, "B001"), // 2021-11-31
            ),
        )

        assertThat(barnetrygdService.harSendtBrevForrigeMåned(listOf(person.fnr), listOf("B001"))).hasSize(0)
    }

    @Test
    fun `harSendtBrevForrigeMåned skal returnere liste med hendleser hvis det er sendt ut brev med kode B001 nylig`() {
        val person = personRepository.saveAndFlush(TestData.person(tkNr = "0312"))
        hendelseRepository.saveAll(
            listOf(
                TestData.hendelse(
                    person,
                    99999999 -
                        LocalDateTime
                            .now()
                            .minusMonths(1)
                            .format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                            .toLong(),
                    "B001",
                ),
            ),
        )

        assertThat(barnetrygdService.harSendtBrevForrigeMåned(listOf(person.fnr), listOf("B001"))).hasSize(1)
    }

    private fun settOppLøpendeOrdinærBarnetrygd(
        stønadStatus: String,
        antallBarn: Int = 1,
    ): Person {
        val person = personRepository.save(TestData.person())
        val løpendeStønad = stonadRepository.save(TestData.stønad(person, status = stønadStatus, opphørtFom = "000000"))
        (1..antallBarn).forEach {
            barnRepository.save(TestData.barn(løpendeStønad))
        }
        sakRepository.save(TestData.sak(person, løpendeStønad.saksblokk, løpendeStønad.sakNr, valg = "OR", undervalg = "OS"))
        utbetalingRepository.saveAll(listOf(TestData.utbetaling(løpendeStønad)))
        return person
    }

    private fun settOppLøpendeUtvidetBarnetrygd(stønadStatus: String = MANUELT_BEREGNET_STATUS): Person {
        val person = personRepository.save(TestData.person())
        val løpendeStønad = stonadRepository.save(TestData.stønad(person, status = stønadStatus, opphørtFom = "000000"))
        barnRepository.save(TestData.barn(løpendeStønad))
        sakRepository.save(TestData.sak(person, løpendeStønad.saksblokk, løpendeStønad.sakNr, valg = "UT", undervalg = "MB"))
        utbetalingRepository.saveAll(
            listOf(
                TestData.utbetaling(løpendeStønad, kontonummer = "06040000", beløp = 660.00), // småbarnstillegg aktiv stønad
                TestData.utbetaling(løpendeStønad), // utvidet på aktiv stønad
            ),
        )
        return person
    }

    private fun leggTilUtgåttUtvidetBarnetrygdSak(
        person: Person,
        beløp: Double? = null,
        stønadStatus: String = "0",
        iverksattFom: String = (999999 - 201905).toString(),
        virkningFom: String = iverksattFom,
        opphørtFom: String = "042020",
        barnFnr: FoedselsNr = TestData.foedselsNr(LocalDate.now()),
    ) {
        val opphørtStønad =
            stonadRepository.save(
                TestData.stønad(
                    person,
                    status = stønadStatus,
                    opphørtFom = opphørtFom,
                    iverksattFom = iverksattFom,
                    virkningFom = virkningFom,
                ),
            )
        sakRepository.save(TestData.sak(person, opphørtStønad.saksblokk, opphørtStønad.sakNr, valg = "UT", undervalg = "MB"))
        if (beløp == null) {
            utbetalingRepository.save(TestData.utbetaling(opphørtStønad))
        } else {
            utbetalingRepository.save(TestData.utbetaling(opphørtStønad, beløp = beløp))
        }
        barnRepository.save(TestData.barn(opphørtStønad, barnFnr))
    }

    companion object {
        const val MANUELT_BEREGNET_STATUS = "0"
        const val ORDINÆR_BARNETRYGD_STATUS = "1"
        const val SATS_BARNETRYGD_OVER_6 = 1054.0
        const val SATS_BARNETRYGD_UNDER_6_2021 = 1654.0
        const val SATS_BARNETRYGD_UNDER_6_2022 = 1676.0
        const val SATS_UTVIDET = 1054.0
    }
}
