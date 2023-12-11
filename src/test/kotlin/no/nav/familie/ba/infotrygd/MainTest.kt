package no.nav.familie.ba.infotrygd

import no.nav.familie.ba.infotrygd.testutil.TestClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class MainTest {

    @LocalServerPort
    var port: Int = 0

    @Autowired
    private lateinit var testClient: TestClient

    @Test
    fun contextLoads() {
    }

    @Test
    fun health() {
        val response = testClient.restTemplateNoAuth(port)
            .getForEntity("/actuator/health", Any::class.java) as ResponseEntity<*>

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }
}
