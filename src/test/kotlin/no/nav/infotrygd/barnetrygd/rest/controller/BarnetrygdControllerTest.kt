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
    private val sakUri = "/infotrygd/barnetrygd/sak"

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

        val client = restClient(port)

        val responseType = InfotrygdSøkResponse::class.java
        val res1 = kallBarnetrygdControllerFor(personUri, client, requestMedPersonSomFinnes).castTo(responseType)
        val res2 = kallBarnetrygdControllerFor(personUri, client, requestMedUkjentPerson).castTo(responseType)
        val res3 = kallBarnetrygdControllerFor(personUri, client, requestMedBarnSomFinnes).castTo(responseType)
        val res4 = kallBarnetrygdControllerFor(personUri, client, requestMedUkjentPersonOgBarn).castTo(responseType)
        val resFraTomRequest = kallBarnetrygdControllerFor(personUri, client).castTo(responseType)

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

        val client = restClient(port)

        val type = InfotrygdSøkResponse::class.java
        val res1 = kallBarnetrygdControllerFor(stønadUri, client, requestMedPersonMedLøpendeSak).castTo(type)
        val res2 = kallBarnetrygdControllerFor(stønadUri, client, requestMedUkjentPerson).castTo(type)
        val res3 = kallBarnetrygdControllerFor(stønadUri, client, requestMedBarnTilknyttetLøpendeSak).castTo(type)
        val res4 = kallBarnetrygdControllerFor(stønadUri, client, requestMedUkjentPersonOgBarn).castTo(type)
        val resFraTomRequest = kallBarnetrygdControllerFor(stønadUri, client).castTo(type)

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

        val client = restClient(port)

        val res = kallBarnetrygdControllerFor(sakUri, client, søkPåPersonMedSak).castTo(SakResponse::class.java)
        Assertions.assertThat(res).extracting { it.saksListe.size }.isEqualTo(1)

    }

    @Test
    fun noAuth() {
        val client = restClientNoAuth(port)
        val result = kallBarnetrygdControllerFor(personUri, client)
        Assertions.assertThat(result.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED)
    }

    @Test
    fun clientAuth() {
        val client = restClient(port, subject = "wrong")
        val result = kallBarnetrygdControllerFor(personUri, client)
        Assertions.assertThat(result.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED)
    }

    private fun kallBarnetrygdControllerFor(
        uri: String,
        client: WebClient,
        request: InfotrygdSøkRequest = InfotrygdSøkRequest(listOf())
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
