package no.nav.infotrygd.beregningsgrunnlag.rest.controller

import no.nav.infotrygd.beregningsgrunnlag.dto.SakResult
import no.nav.infotrygd.beregningsgrunnlag.repository.VedtakRepository
import no.nav.infotrygd.beregningsgrunnlag.testutil.TestData
import no.nav.infotrygd.beregningsgrunnlag.testutil.restClient
import no.nav.infotrygd.beregningsgrunnlag.testutil.restClientNoAuth
import org.assertj.core.api.Assertions
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import java.time.LocalDate

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class PaaroerendeSykdomSakControllerTest {

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    lateinit var vedtakRepository: VedtakRepository

    private val fnr = TestData.foedselsNr()
    private val queryString = "fnr=${fnr.asString}&fom=2018-01-01"
    private val uri = "/paaroerendeSykdom/sak?$queryString"

    @Test
    fun barnSykdom() {
        val vedtak = TestData.vedtak(
            datoStart = LocalDate.now(),
            fnr = fnr,
            delytelserEksermpler = listOf(TestData.delytelse())
        )

        vedtakRepository.save(vedtak)

        val client = restClient(port)
        val result = client.get()
            .uri(uri)
            .exchange()
            .block() !!
            .bodyToMono(SakResult::class.java)
            .block() !!

        Assertions.assertThat(result.vedtak).hasSize(1)
    }

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