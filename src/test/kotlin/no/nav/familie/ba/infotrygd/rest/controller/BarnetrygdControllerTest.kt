package no.nav.familie.ba.infotrygd.rest.controller

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.familie.ba.infotrygd.model.db2.Beslutning
import no.nav.familie.ba.infotrygd.model.db2.Endring
import no.nav.familie.ba.infotrygd.model.db2.LøpeNrFnr
import no.nav.familie.ba.infotrygd.model.db2.StønadDb2
import no.nav.familie.ba.infotrygd.repository.BarnRepository
import no.nav.familie.ba.infotrygd.repository.BeslutningRepository
import no.nav.familie.ba.infotrygd.repository.EndringRepository
import no.nav.familie.ba.infotrygd.repository.LøpeNrFnrRepository
import no.nav.familie.ba.infotrygd.repository.PersonRepository
import no.nav.familie.ba.infotrygd.repository.SakPersonRepository
import no.nav.familie.ba.infotrygd.repository.SakRepository
import no.nav.familie.ba.infotrygd.repository.StønadDb2Repository
import no.nav.familie.ba.infotrygd.repository.StønadRepository
import no.nav.familie.ba.infotrygd.repository.UtbetalingRepository
import no.nav.familie.ba.infotrygd.repository.VedtakRepository
import no.nav.familie.ba.infotrygd.rest.api.InfotrygdLøpendeBarnetrygdResponse
import no.nav.familie.ba.infotrygd.rest.api.InfotrygdSøkRequest
import no.nav.familie.ba.infotrygd.rest.api.InfotrygdÅpenSakResponse
import no.nav.familie.ba.infotrygd.rest.controller.BarnetrygdController.StønadRequest
import no.nav.familie.ba.infotrygd.service.BarnetrygdService
import no.nav.familie.ba.infotrygd.testutil.TestClient
import no.nav.familie.ba.infotrygd.testutil.TestData
import no.nav.familie.kontrakter.ba.infotrygd.InfotrygdSøkResponse
import no.nav.familie.kontrakter.ba.infotrygd.Sak
import no.nav.familie.kontrakter.felles.objectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.assertThrows
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.web.client.RestTemplate
import org.springframework.web.server.ResponseStatusException
import no.nav.familie.kontrakter.ba.infotrygd.Stønad as StønadDto


@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class BarnetrygdControllerTest {

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var testClient: TestClient

    private lateinit var restTemplate: RestTemplate

    @Autowired
    lateinit var personRepository: PersonRepository

    @Autowired
    lateinit var stønadRepository: StønadRepository

    @Autowired
    lateinit var barnRepository: BarnRepository

    @Autowired
    lateinit var sakRepository: SakRepository

    @Autowired
    lateinit var sakPersonRepository: SakPersonRepository

    @Autowired
    lateinit var vedtakRepository: VedtakRepository

    @Autowired
    lateinit var løpeNrFnrRepository: LøpeNrFnrRepository

    @Autowired
    lateinit var stønadDb2Repository: StønadDb2Repository

    @Autowired
    lateinit var endringRepository: EndringRepository

    @Autowired
    lateinit var beslutningRepository: BeslutningRepository

    @Autowired
    lateinit var barnetrygdService: BarnetrygdService

    @Autowired
    lateinit var utbetalingRepository: UtbetalingRepository

    private val uri = mapOf("stønad" to "/infotrygd/barnetrygd/stonad",
                            "sak" to "/infotrygd/barnetrygd/saker",
                            "lopende-barnetrygd" to "/infotrygd/barnetrygd/lopende-barnetrygd",
                            "aapen-sak" to "/infotrygd/barnetrygd/aapen-sak")

    @Before
    fun init() {
        restTemplate = testClient.restTemplate(port)
    }

    @Test
    fun `infotrygdsøk etter løpende barnetrygd`() {
        val (person, opphørPerson) = personRepository.saveAll(listOf(1,2).map { TestData.person() })

        val barn = barnRepository.saveAndFlush(TestData.barn(person))

        stønadRepository.saveAll(listOf(TestData.stønad(person), TestData.stønad(opphørPerson, opphørtFom = "111111")))

        val requestMedPersonMedLøpendeStønad = InfotrygdSøkRequest(listOf(person.fnr))
        val requestMedPersonMedOpphørtStønad = InfotrygdSøkRequest(listOf(opphørPerson.fnr))
        val requestMedBarnTilknyttetLøpendeStønad = InfotrygdSøkRequest(listOf(opphørPerson.fnr), listOf(barn.barnFnr))
        val requestMedBarnSomIkkeFinnes = InfotrygdSøkRequest(listOf(), listOf(person.fnr))

        assertThat(post(requestMedPersonMedLøpendeStønad, uri["lopende-barnetrygd"], InfotrygdLøpendeBarnetrygdResponse::class.java)
            .harLøpendeBarnetrygd).isTrue
        assertThat(post(requestMedPersonMedLøpendeStønad, uri["stønad"], InfotrygdSøkResponse::class.java).bruker)
            .isNotEmpty
        assertThat(post(requestMedPersonMedOpphørtStønad, uri["stønad"], InfotrygdSøkResponse::class.java)).extracting("bruker", "barn")
            .containsOnly(emptyList<StønadDto>())
        assertThat(post(requestMedBarnTilknyttetLøpendeStønad, uri["stønad"], InfotrygdSøkResponse::class.java).barn)
            .isNotEmpty
        assertThat(post(requestMedBarnSomIkkeFinnes, uri["stønad"], InfotrygdSøkResponse::class.java)).extracting("bruker", "barn")
            .containsOnly(emptyList<StønadDto>())
        assertThat(post(uri = uri["stønad"], responseType = InfotrygdSøkResponse::class.java)).extracting("bruker", "barn")
            .containsOnly(emptyList<StønadDto>())
    }

    @Test
    fun `infotrygdsøk etter saker by fnr`() {
        val person = personRepository.saveAndFlush(TestData.person())
        val sak = sakRepository.saveAndFlush(TestData.sak(person))
        sakPersonRepository.saveAndFlush(TestData.sakPerson(person))
        val barn = barnRepository.saveAndFlush(TestData.barn(person))

        val søkPåPersonMedSak = InfotrygdSøkRequest(listOf(person.fnr))
        val søkPåBarnTilknyttetSak = InfotrygdSøkRequest(listOf(), listOf(barn.barnFnr))

        assertThat(post(søkPåPersonMedSak, uri["sak"], InfotrygdSøkResponse::class.java)).extracting {
                it -> it.bruker.map { objectMapper.convertValue(it, Sak::class.java) }
        }.isEqualToComparingFieldByFieldRecursively(listOf(barnetrygdService.konverterTilDto(sak)))

        assertThat(post(søkPåBarnTilknyttetSak, uri["sak"], InfotrygdSøkResponse::class.java)).extracting {
                it -> it.barn.map { objectMapper.convertValue(it, Sak::class.java) }
        }.isEqualToComparingFieldByFieldRecursively(listOf(barnetrygdService.konverterTilDto(sak)))

        assertThat(post(uri = uri["sak"], responseType = InfotrygdSøkResponse::class.java).bruker) // søk med tom request
            .isEmpty()
    }

    @Test
    fun `aapen-sak skal svare true når det finnes sak med vedtak uten beslutning, deretter false etter beslutning`() {
        val person = personRepository.saveAndFlush(TestData.person()).also {
            løpeNrFnrRepository.saveAndFlush(LøpeNrFnr(1, it.fnr.asString))
        }
        val sak = sakRepository.saveAndFlush(TestData.sak(person))

        val vedtak = vedtakRepository.saveAndFlush(TestData.vedtak(sak)).also {
            stønadDb2Repository.saveAndFlush(StønadDb2(it.stønadId, "BA", 1))
            endringRepository.saveAndFlush(Endring(it.vedtakId, "  "))
        }

        val søkRequest = InfotrygdSøkRequest(listOf(person.fnr), emptyList())

        assertThat(post(søkRequest, uri["aapen-sak"], InfotrygdÅpenSakResponse::class.java).harÅpenSak)
            .isTrue

        beslutningRepository.saveAndFlush(Beslutning(1, vedtak.vedtakId, "J"))

        assertThat(post(søkRequest, uri["aapen-sak"], InfotrygdÅpenSakResponse::class.java).harÅpenSak)
            .isFalse
    }

    @Test
    fun `skal finne riktig antall personer med utvidet barnetrygd året 2020`() {
        stønadRepository.saveAll(listOf(
            TestData.stønad(TestData.person(), virkningFom = (999999-201901).toString(), status = "01"), // ordinær barnetrygd fra 2019
            TestData.stønad(TestData.person(), status = "02"), // utvidet barnetrygd fra 2020-05 (by default)
            TestData.stønad(TestData.person(), opphørtFom = "122020", status = "02") // utvidet barnetrygd kun 2020
        )).also { stønader ->
            utbetalingRepository.saveAll(stønader.map { TestData.utbetaling(it) })
        }

        get("/infotrygd/barnetrygd/utvidet?aar=2020",
            BisysController.InfotrygdUtvidetBaPersonerResponse::class.java).also {
                assertThat(it.brukere).hasSize(2)
            }
    }


    @Test
    fun `skal hente stønad basert på personKey, iverksattFom, virkningFom og region`() {
        val personIdent = "12345678910"
        val personKey = "031256341278910"
        val person =
            TestData.person(fnr = FoedselsNr(personIdent), personKey = personKey.toLong(), tkNr = personKey.substring(0, 4))
        val stønad = stønadRepository.saveAndFlush(TestData.stønad(person))

        post(
            uri = "/infotrygd/barnetrygd/stonad/sok",
            request = StønadRequest(
                person.fnr.asString,
                stønad.tkNr,
                stønad.iverksattFom,
                stønad.virkningFom,
                stønad.region
            ),
            responseType = StønadDto::class.java
        ).also {
            assertThat(it.id).isEqualTo(stønad.id)
        }
    }

    @Test
    fun `hent stønad basert på id returnerer 404 hvis stønad ikke eksisterer`() {
        val stønad = stønadRepository.saveAndFlush(
            TestData.stønad(TestData.person(), virkningFom = (999999-201901).toString(), status = "01"), // ordinær barnetrygd fra 2019
        )

        val response = assertThrows<ResponseStatusException> {
            get("/infotrygd/barnetrygd/stonad/666", Any::class.java)
        }
        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    }

    @Test
    fun noAuth() {
        uri.values.forEach {
            val restTemplate = testClient.restTemplateNoAuth(port)
            val result = assertThrows<ResponseStatusException> {
                post(uri = it, restTemplate = restTemplate, responseType = InfotrygdSøkResponse::class.java)
            }
            assertThat(result.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
        }
    }

    private fun <T> post(
        request: Any = InfotrygdSøkRequest(listOf()),
        uri: String?,
        responseType: Class<T>,
        restTemplate: RestTemplate = this.restTemplate,
    ) = restTemplate.postForEntity(uri!!, request, responseType).body!!

    private fun <T> get(
        uri: String?,
        responseType: Class<T>
    ) = restTemplate.getForEntity(uri!!, responseType).body!!
}
