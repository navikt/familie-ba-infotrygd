package no.nav.infotrygd.barnetrygd.rest.controller

import no.nav.infotrygd.barnetrygd.testutil.TestData
import no.nav.infotrygd.barnetrygd.testutil.restClient
import no.nav.infotrygd.barnetrygd.testutil.restClientNoAuth
import org.assertj.core.api.Assertions
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class RammevedtakOmsorgspengerControllerTest {

    @LocalServerPort
    private var port: Int = 0

    private val fnr = TestData.foedselsNr()

    private val queryString = "fnr=${fnr.asString}&fom=2018-01-01"
    private val uri = "/rammevedtak/omsorgspenger?$queryString"

    @Test
    fun noAuth() {
        val client = restClientNoAuth(port)
        val result = client.get()
            .uri(uri)
            .exchange()
            .block() !!
        Assertions.assertThat(result.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED)
    }

    @Test
    fun clientAuth() {
        val client = restClient(port, subject = "wrong")
        val result = client.get()
            .uri(uri)
            .exchange()
            .block() !!
        Assertions.assertThat(result.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED)
    }
}