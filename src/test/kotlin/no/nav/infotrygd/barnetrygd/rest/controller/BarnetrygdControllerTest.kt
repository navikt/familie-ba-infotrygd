package no.nav.infotrygd.barnetrygd.rest.controller

import no.nav.infotrygd.barnetrygd.repository.PersonRepository
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

    private val uri = "/infotrygd/barnetrygd/personsok"

    @Test
    fun infotrygdSøk() {
        val person = TestData.person()
        val ukjentPerson = TestData.person()
        val barn = TestData.barn(person)

        personRepository.saveAndFlush(person.copy(barn = listOf(barn)))

        val requestMedPersonSomFinnes = InfotrygdSøkRequest(listOf(person.fnr))
        val requestMedUkjentPerson = InfotrygdSøkRequest(listOf(ukjentPerson.fnr))
        val requestMedBarnSomFinnes = InfotrygdSøkRequest(listOf(ukjentPerson.fnr), listOf(barn.barnFnr))
        val requestMedUkjentPersonOgBarn = InfotrygdSøkRequest(listOf(ukjentPerson.fnr), listOf(person.fnr))

        val client = restClient(port)

        val res1 = kallBarnetrygdControllerFor(client, requestMedPersonSomFinnes).responseBody()
        val res2 = kallBarnetrygdControllerFor(client, requestMedUkjentPerson).responseBody()
        val res3 = kallBarnetrygdControllerFor(client, requestMedBarnSomFinnes).responseBody()
        val res4 = kallBarnetrygdControllerFor(client, requestMedUkjentPersonOgBarn).responseBody()
        val resFraTomRequest = kallBarnetrygdControllerFor(client)

        Assertions.assertThat(res1.ingenTreff).isFalse()
        Assertions.assertThat(res2.ingenTreff).isTrue()
        Assertions.assertThat(res3.ingenTreff).isFalse()
        Assertions.assertThat(res4.ingenTreff).isTrue()
        Assertions.assertThat(resFraTomRequest.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun noAuth() {
        val client = restClientNoAuth(port)
        val result = kallBarnetrygdControllerFor(client)
        Assertions.assertThat(result.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED)
    }

    @Test
    fun clientAuth() {
        val client = restClient(port, subject = "wrong")
        val result = kallBarnetrygdControllerFor(client)
        Assertions.assertThat(result.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED)
    }

    private fun kallBarnetrygdControllerFor(
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
