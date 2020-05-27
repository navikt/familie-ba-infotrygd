package no.nav.infotrygd.barnetrygd.rest.controller

import no.nav.infotrygd.barnetrygd.dto.VedtakPleietrengendeDto
import no.nav.infotrygd.barnetrygd.repository.VedtakRepository
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
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import java.time.LocalDate

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class VedtakForPleietrengendeControllerTest {

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    lateinit var vedtakRepository: VedtakRepository

    private val barnFnr = TestData.foedselsNr()

    private val queryString = "fnr=${barnFnr.asString}&fom=2018-01-01"
    private val uri = "/vedtakForPleietrengende?$queryString"

    @Test
    fun vedtakForPleietrengende() {
        val vedtak = TestData.vedtak(
            datoStart = LocalDate.now(),
            stonad = TestData.stonad(TestData.stonadBs(fnrBarn = barnFnr)),
            delytelserEksermpler = listOf(TestData.delytelse())
        )

        vedtakRepository.save(vedtak)

        val client = restClient(port)
        val result = client.get()
            .uri(uri)
            .exchange()
            .block() !!
            .bodyToMono(Array<VedtakPleietrengendeDto>::class.java)
            .block() !!

        Assertions.assertThat(result).hasSize(1)
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