package no.nav.infotrygd.barnetrygd.rest.controller

import no.nav.familie.kontrakter.ba.infotrygd.InfotrygdSøkResponse
import no.nav.familie.kontrakter.ba.infotrygd.Sak
import no.nav.familie.kontrakter.felles.objectMapper
import no.nav.infotrygd.barnetrygd.model.db2.Beslutning
import no.nav.infotrygd.barnetrygd.model.db2.Endring
import no.nav.infotrygd.barnetrygd.model.db2.LøpeNrFnr
import no.nav.infotrygd.barnetrygd.model.db2.StønadDb2
import no.nav.infotrygd.barnetrygd.repository.*
import no.nav.infotrygd.barnetrygd.rest.api.InfotrygdLøpendeBarnetrygdResponse
import no.nav.infotrygd.barnetrygd.rest.api.InfotrygdSøkRequest
import no.nav.infotrygd.barnetrygd.rest.api.InfotrygdÅpenSakResponse
import no.nav.infotrygd.barnetrygd.service.BarnetrygdService
import no.nav.infotrygd.barnetrygd.testutil.TestData
import no.nav.infotrygd.barnetrygd.testutil.restClient
import no.nav.infotrygd.barnetrygd.testutil.restClientNoAuth
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import no.nav.familie.kontrakter.ba.infotrygd.Stønad as StønadDto

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class BarnetrygdControllerTest {

    @LocalServerPort
    private var port: Int = 0

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

    private val uri = mapOf("stønad" to "/infotrygd/barnetrygd/stonad",
                            "sak" to "/infotrygd/barnetrygd/saker",
                            "deprecated" to "/infotrygd/barnetrygd/lopendeSak",
                            "lopende-barnetrygd" to "/infotrygd/barnetrygd/lopende-barnetrygd",
                            "aapen-sak" to "/infotrygd/barnetrygd/aapen-sak")

    @Test
    fun `infotrygdsøk etter løpende barnetrygd`() {
        val (person, opphørPerson) = personRepository.saveAll(listOf(1,2).map { TestData.person() })

        val barn = barnRepository.saveAndFlush(TestData.barn(person))

        stønadRepository.saveAll(listOf(TestData.stønad(person), TestData.stønad(opphørPerson, opphørtFom = "111111")))

        val requestMedPersonMedLøpendeStønad = InfotrygdSøkRequest(listOf(person.fnr))
        val requestMedPersonMedOpphørtStønad = InfotrygdSøkRequest(listOf(opphørPerson.fnr))
        val requestMedBarnTilknyttetLøpendeStønad = InfotrygdSøkRequest(listOf(opphørPerson.fnr), listOf(barn.barnFnr))
        val requestMedBarnSomIkkeFinnes = InfotrygdSøkRequest(listOf(), listOf(person.fnr))

        assertThat(post(requestMedPersonMedLøpendeStønad, uri["lopende-barnetrygd"]).pakkUt(InfotrygdLøpendeBarnetrygdResponse::class.java)
            .harLøpendeBarnetrygd).isTrue
        assertThat(post(requestMedPersonMedLøpendeStønad, uri["stønad"]).pakkUt(InfotrygdSøkResponse::class.java).bruker)
            .isNotEmpty
        assertThat(post(requestMedPersonMedOpphørtStønad, uri["stønad"]).pakkUt(InfotrygdSøkResponse::class.java)).extracting("bruker", "barn")
            .containsOnly(emptyList<StønadDto>())
        assertThat(post(requestMedBarnTilknyttetLøpendeStønad, uri["stønad"]).pakkUt(InfotrygdSøkResponse::class.java).barn)
            .isNotEmpty
        assertThat(post(requestMedBarnSomIkkeFinnes, uri["stønad"]).pakkUt(InfotrygdSøkResponse::class.java)).extracting("bruker", "barn")
            .containsOnly(emptyList<StønadDto>())
        assertThat(post(uri = uri["stønad"]).pakkUt(InfotrygdSøkResponse::class.java)).extracting("bruker", "barn")
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

        assertThat(post(søkPåPersonMedSak, uri["sak"]).pakkUt(InfotrygdSøkResponse::class.java)).extracting {
                it -> it.bruker.map { objectMapper.convertValue(it, Sak::class.java) }
        }.isEqualToComparingFieldByFieldRecursively(listOf(barnetrygdService.konverterTilDto(sak)))

        assertThat(post(søkPåBarnTilknyttetSak, uri["sak"]).pakkUt(InfotrygdSøkResponse::class.java)).extracting {
                it -> it.barn.map { objectMapper.convertValue(it, Sak::class.java) }
        }.isEqualToComparingFieldByFieldRecursively(listOf(barnetrygdService.konverterTilDto(sak)))

        assertThat(post(uri = uri["sak"]).pakkUt(InfotrygdSøkResponse::class.java).bruker) // søk med tom request
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

        assertThat(post(søkRequest, uri["aapen-sak"]).pakkUt(InfotrygdÅpenSakResponse::class.java).harÅpenSak)
            .isTrue

        beslutningRepository.saveAndFlush(Beslutning(1, vedtak.vedtakId, "J"))

        assertThat(post(søkRequest, uri["aapen-sak"]).pakkUt(InfotrygdÅpenSakResponse::class.java).harÅpenSak)
            .isFalse
    }

    @Test
    fun `skal finne riktig antall personer med utvidet barnetrygd året 2020`() {
        stønadRepository.saveAll(listOf(
            TestData.stønad(TestData.person(), virkningFom = (999999-201901).toString(), status = "01"), // ordinær barnetrygd fra 2019
            TestData.stønad(TestData.person(), status = "02"), // utvidet barnetrygd fra 2020 (by default)
            TestData.stønad(TestData.person(), opphørtFom = "122020", status = "02") // utvidet barnetrygd kun 2020
        ))

        get("/infotrygd/barnetrygd/utvidet?aar=2020")
            .pakkUt(BarnetrygdController.InfotrygdUtvidetBaPersonerResponse::class.java).also {
                assertThat(it.brukere).hasSize(2)
            }
    }

    @Test
    fun noAuth() {
        uri.values.forEach {
            val client = restClientNoAuth(port)
            val result = post(uri = it, client = client)
            assertThat(result.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED)
        }
    }

    @Test
    fun clientAuth() {
        uri.values.forEach {
            val client = restClient(port, subject = "wrong")
            val result = post(uri = it, client = client)
            assertThat(result.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED)
        }
    }

    private fun post(
        request: InfotrygdSøkRequest = InfotrygdSøkRequest(listOf()),
        uri: String?,
        client: WebClient = restClient(port),
    ): ClientResponse {
        return client.post()
            .uri(uri!!)
            .contentType(MediaType.APPLICATION_JSON)
            .syncBody(request)
            .exchange()
            .block()!!
    }

    private fun get(uri: String?): ClientResponse {
        return restClient(port)
            .get()
            .uri(uri!!)
            .exchange()
            .block() !!
    }
}

private fun <T> ClientResponse.pakkUt(type: Class<T>): T {
    return this.bodyToMono(type).block()!!
}