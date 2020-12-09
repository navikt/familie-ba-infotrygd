package no.nav.infotrygd.barnetrygd.rest.controller

import no.nav.infotrygd.barnetrygd.repository.BarnRepository
import no.nav.infotrygd.barnetrygd.repository.PersonRepository
import no.nav.infotrygd.barnetrygd.repository.SakRepository
import no.nav.infotrygd.barnetrygd.repository.StønadRepository
import no.nav.infotrygd.barnetrygd.rest.api.InfotrygdSøkRequest
import no.nav.infotrygd.barnetrygd.rest.api.InfotrygdSøkResponse
import no.nav.infotrygd.barnetrygd.testutil.TestData
import no.nav.infotrygd.barnetrygd.testutil.restClient
import no.nav.infotrygd.barnetrygd.testutil.restClientNoAuth
import org.assertj.core.api.Assertions
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

    private val personUri = "/infotrygd/barnetrygd/personsok"
    private val stønadUri = "/infotrygd/barnetrygd/lopendeSak"
    private val sakUri = "/infotrygd/barnetrygd/saker"

    @Test
    fun `infotrygd historikk søk`() {
        val person = TestData.person()
        val ukjentPerson = TestData.person()
        val barn = TestData.barn(person)

        personRepository.saveAndFlush(person)
        barnRepository.saveAndFlush(barn)

        val requestMedPersonSomFinnes = InfotrygdSøkRequest(listOf(person.fnr))
        val requestMedUkjentPerson = InfotrygdSøkRequest(listOf(ukjentPerson.fnr))
        val requestMedBarnSomFinnes = InfotrygdSøkRequest(listOf(ukjentPerson.fnr), listOf(barn.barnFnr))
        val requestMedUkjentPersonOgBarn = InfotrygdSøkRequest(listOf(ukjentPerson.fnr), listOf(person.fnr))

        val responseType = InfotrygdSøkResponse::class.java
        val res1 = post(requestMedPersonSomFinnes, personUri).castTo(responseType)
        val res2 = post(requestMedUkjentPerson, personUri).castTo(responseType)
        val res3 = post(requestMedBarnSomFinnes, personUri).castTo(responseType)
        val res4 = post(requestMedUkjentPersonOgBarn, personUri).castTo(responseType)
        val resFraTomRequest = post(uri = personUri).castTo(responseType)

        Assertions.assertThat(res1.ingenTreff).isFalse()
        Assertions.assertThat(res2.ingenTreff).isTrue()
        Assertions.assertThat(res3.ingenTreff).isFalse()
        Assertions.assertThat(res4.ingenTreff).isTrue()
        Assertions.assertThat(resFraTomRequest.ingenTreff).isTrue()
    }

    @Test
    fun `infotrygdsøk etter løpende barnetrygd`() {
        val person = TestData.person()
        val ukjentPerson = TestData.person()
        val stønad = TestData.stønad(person)
        val stønad2 = TestData.stønad(ukjentPerson, opphørtFom = "111111")
        val barn = TestData.barn(person)

        personRepository.saveAndFlush(person)
        stønadRepository.saveAll(listOf(stønad, stønad2))
        barnRepository.saveAndFlush(barn)

        val requestMedPersonMedLøpendeSak = InfotrygdSøkRequest(listOf(person.fnr))
        val requestMedUkjentPerson = InfotrygdSøkRequest(listOf(ukjentPerson.fnr))
        val requestMedBarnTilknyttetLøpendeSak = InfotrygdSøkRequest(listOf(ukjentPerson.fnr), listOf(barn.barnFnr))
        val requestMedUkjentPersonOgBarn = InfotrygdSøkRequest(listOf(ukjentPerson.fnr), listOf(person.fnr))

        val responseType = InfotrygdSøkResponse::class.java
        val res1 = post(requestMedPersonMedLøpendeSak, stønadUri).castTo(responseType)
        val res2 = post(requestMedUkjentPerson, stønadUri).castTo(responseType)
        val res3 = post(requestMedBarnTilknyttetLøpendeSak, stønadUri).castTo(responseType)
        val res4 = post(requestMedUkjentPersonOgBarn, stønadUri).castTo(responseType)
        val resFraTomRequest = post(uri = stønadUri).castTo(responseType)

        Assertions.assertThat(res1.ingenTreff).isFalse()
        Assertions.assertThat(res2.ingenTreff).isTrue()
        Assertions.assertThat(res3.ingenTreff).isFalse()
        Assertions.assertThat(res4.ingenTreff).isTrue()
        Assertions.assertThat(resFraTomRequest.ingenTreff).isTrue()
    }

    @Test
    fun bar() {
        val person = TestData.person()
        personRepository.saveAndFlush(person)
        sakRepository.saveAndFlush(TestData.sak(person))

        val søkPåPersonMedSak = InfotrygdSøkRequest(listOf(person.fnr))

        val res = post(søkPåPersonMedSak, sakUri).castTo(SakResponse::class.java)
        val tomRequest = post(uri = sakUri).castTo(SakResponse::class.java)

        Assertions.assertThat(res).extracting { it.saker.size }.isEqualTo(1)
        Assertions.assertThat(tomRequest.saker).isEmpty()
    }

    @Test
    fun noAuth() {
        val client = restClientNoAuth(port)
        val result = post(uri = personUri, client = client)
        Assertions.assertThat(result.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED)
    }

    @Test
    fun clientAuth() {
        val client = restClient(port, subject = "wrong")
        val result = post(uri = personUri, client = client)
        Assertions.assertThat(result.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED)
    }

    private fun post(
        request: InfotrygdSøkRequest = InfotrygdSøkRequest(listOf()),
        uri: String,
        client: WebClient = restClient(port),
    ): ClientResponse {
        return client.post()
            .uri(uri)
            .contentType(MediaType.APPLICATION_JSON)
            .syncBody(request)
            .exchange()
            .block()!!
    }
}

private fun <T> ClientResponse.castTo(type: Class<T>): T {
    return this.bodyToMono(type).block()!!
}
