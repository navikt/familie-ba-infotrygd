package no.nav.infotrygd.barnetrygd.rest.controller

import no.nav.infotrygd.barnetrygd.repository.BarnRepository
import no.nav.infotrygd.barnetrygd.repository.PersonRepository
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

    private val uri = "/infotrygd/barnetrygd/personsok"
    private val uri2 = "/infotrygd/barnetrygd/lopendeSak"

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

        val res1 = kallBarnetrygdControllerFor(uri, client, requestMedPersonSomFinnes).responseBody()
        val res2 = kallBarnetrygdControllerFor(uri, client, requestMedUkjentPerson).responseBody()
        val res3 = kallBarnetrygdControllerFor(uri, client, requestMedBarnSomFinnes).responseBody()
        val res4 = kallBarnetrygdControllerFor(uri, client, requestMedUkjentPersonOgBarn).responseBody()
        val resFraTomRequest = kallBarnetrygdControllerFor(uri, client).responseBody()

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

        val res1 = kallBarnetrygdControllerFor(uri2, client, requestMedPersonMedLøpendeSak).responseBody()
        val res2 = kallBarnetrygdControllerFor(uri2, client, requestMedUkjentPerson).responseBody()
        val res3 = kallBarnetrygdControllerFor(uri2, client, requestMedBarnTilknyttetLøpendeSak).responseBody()
        val res4 = kallBarnetrygdControllerFor(uri2, client, requestMedUkjentPersonOgBarn).responseBody()
        val resFraTomRequest = kallBarnetrygdControllerFor(uri2, client).responseBody()

        Assertions.assertThat(res1.ingenTreff).isFalse()
        Assertions.assertThat(res2.ingenTreff).isTrue()
        Assertions.assertThat(res3.ingenTreff).isFalse()
        Assertions.assertThat(res4.ingenTreff).isTrue()
        Assertions.assertThat(resFraTomRequest.ingenTreff).isTrue()
    }

    @Test
    fun noAuth() {
        val client = restClientNoAuth(port)
        val result = kallBarnetrygdControllerFor(uri, client)
        Assertions.assertThat(result.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED)
    }

    @Test
    fun clientAuth() {
        val client = restClient(port, subject = "wrong")
        val result = kallBarnetrygdControllerFor(uri, client)
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

private fun ClientResponse.responseBody(): InfotrygdSøkResponse {
    return this.bodyToMono(InfotrygdSøkResponse::class.java).block()!!
}
