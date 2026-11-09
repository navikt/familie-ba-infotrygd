package no.nav.familie.ba.infotrygd

import no.nav.familie.ba.infotrygd.testutil.MockOAuth2ServerInitializer
import no.nav.familie.ba.infotrygd.testutil.TestClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.web.server.ResponseStatusException

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration(initializers = [MockOAuth2ServerInitializer::class])
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
        val response =
            testClient
                .restTemplateNoAuth(port)
                .getForEntity("/actuator/health", Any::class.java) as ResponseEntity<*>

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }

    @ParameterizedTest
    @ValueSource(strings = ["/internal/info", "/internal/mappings", "/internal/status/isAlive", "/internal/healthcheck", "/internal/prometheus/other"])
    fun `skal avvise øvrige internal-endepunkter uten token`(path: String) {
        val feil =
            assertThrows<ResponseStatusException> {
                testClient.restTemplateNoAuth(port).getForEntity(path, String::class.java)
            }

        assertThat(feil.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
    }
}
