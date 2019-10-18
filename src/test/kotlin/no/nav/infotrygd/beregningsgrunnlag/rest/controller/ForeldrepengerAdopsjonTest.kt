package no.nav.infotrygd.beregningsgrunnlag.rest.controller

import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Stoenadstype
import no.nav.infotrygd.beregningsgrunnlag.repository.PeriodeRepository
import no.nav.infotrygd.beregningsgrunnlag.testutil.TestData
import no.nav.infotrygd.beregningsgrunnlag.testutil.restClient
import no.nav.infotrygd.beregningsgrunnlag.testutil.restClientNoAuth
import org.assertj.core.api.Assertions.assertThat
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
class ForeldrepengerAdopsjonTest {

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var periodeRepository: PeriodeRepository

    private val fnr = TestData.foedselsNr()
    private val queryString = "fodselNr=${fnr.asString}&fom=2018-01-01"
    val uri = "/foreldrepenger/adopsjon?$queryString"

    @Test
    fun adopsjon() {

        val pf = TestData.PeriodeFactory(fnr = fnr)

        val periode = pf.periode().copy(
            stoenadstype = Stoenadstype.ADOPSJON,
            foedselsdatoBarn = LocalDate.now().minusYears(1)
        )
        periodeRepository.save(periode)

        val client = restClient(port)
        val result = client.get()
            .uri(uri)
            .exchange()
            .block() !!
            .bodyToMono(Array<Any>::class.java)
            .block() !!

        assertThat(result).hasSize(1)
    }

    @Test
    fun adopsjonNoAuth() {
        val client = restClientNoAuth(port)
        val result = client.get()
            .uri(uri)
            .exchange()
            .block() !!
        assertThat(result.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED)
    }

    @Test
    fun adopsjonClientAuth() {
        val client = restClient(port, subject = "wrong")
        val result = client.get()
            .uri(uri)
            .exchange()
            .block() !!
        assertThat(result.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED)
    }
}