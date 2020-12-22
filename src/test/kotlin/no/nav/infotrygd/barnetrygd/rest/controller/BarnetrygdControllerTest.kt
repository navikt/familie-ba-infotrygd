package no.nav.infotrygd.barnetrygd.rest.controller

import no.nav.infotrygd.barnetrygd.repository.BarnRepository
import no.nav.infotrygd.barnetrygd.repository.PersonRepository
import no.nav.infotrygd.barnetrygd.repository.SakRepository
import no.nav.infotrygd.barnetrygd.repository.StønadRepository
import no.nav.infotrygd.barnetrygd.rest.api.*
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

    private val uri = mapOf("stønad" to "/infotrygd/barnetrygd/stonad",
                            "sak" to "/infotrygd/barnetrygd/saker",
                            "deprecated" to "/infotrygd/barnetrygd/lopendeSak")

    @Test
    fun `infotrygdsøk etter løpende barnetrygd`() {
        val (person, opphørPerson) = personRepository.saveAll(listOf(1,2).map { TestData.person() })
        val barn = barnRepository.saveAndFlush(TestData.barn(person))

        stønadRepository.saveAll(listOf(TestData.stønad(person), TestData.stønad(opphørPerson, opphørtFom = "111111")))

        val requestMedPersonMedLøpendeStønad = InfotrygdSøkRequest(listOf(person.fnr))
        val requestMedPersonMedOpphørtStønad = InfotrygdSøkRequest(listOf(opphørPerson.fnr))
        val requestMedBarnTilknyttetLøpendeStønad = InfotrygdSøkRequest(listOf(opphørPerson.fnr), listOf(barn.barnFnr))
        val requestMedBarnSomIkkeFinnes = InfotrygdSøkRequest(listOf(), listOf(person.fnr))

        val responseType = StønadResult::class.java

        assertThat(post(requestMedPersonMedLøpendeStønad, uri["stønad"]).pakkUt(responseType).bruker)
            .isNotEmpty
        assertThat(post(requestMedPersonMedOpphørtStønad, uri["stønad"]).pakkUt(responseType)).extracting("bruker", "barn")
            .containsOnly(emptyList<StønadDto>())
        assertThat(post(requestMedBarnTilknyttetLøpendeStønad, uri["stønad"]).pakkUt(responseType).barn)
            .isNotEmpty
        assertThat(post(requestMedBarnSomIkkeFinnes, uri["stønad"]).pakkUt(responseType)).extracting("bruker", "barn")
            .containsOnly(emptyList<StønadDto>())
        assertThat(post(uri = uri["stønad"]).pakkUt(responseType)).extracting("bruker", "barn")
            .containsOnly(emptyList<StønadDto>())
    }

    @Test
    fun `infotrygdsøk etter saker by fnr`() {
        val person = personRepository.saveAndFlush(TestData.person())
        val barn = barnRepository.saveAndFlush(TestData.barn(person))
        val sak = sakRepository.saveAndFlush(TestData.sak(person))

        val søkPåPersonMedSak = InfotrygdSøkRequest(listOf(person.fnr))
        val søkPåBarnTilknyttetSak = InfotrygdSøkRequest(listOf(), listOf(barn.barnFnr))

        assertThat(post(søkPåPersonMedSak, uri["sak"]).pakkUt(SakResult::class.java)).extracting { it.bruker }
            .isEqualToComparingFieldByFieldRecursively(listOf(sak.toSakDto()))
        assertThat(post(søkPåBarnTilknyttetSak, uri["sak"]).pakkUt(SakResult::class.java)).extracting { it.barn }
            .isEqualToComparingFieldByFieldRecursively(listOf(sak.toSakDto()))
        assertThat(post(uri = uri["sak"]).pakkUt(SakResult::class.java).bruker) // søk med tom request
            .isEmpty()
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
}

private fun <T> ClientResponse.pakkUt(type: Class<T>): T {
    return this.bodyToMono(type).block()!!
}
