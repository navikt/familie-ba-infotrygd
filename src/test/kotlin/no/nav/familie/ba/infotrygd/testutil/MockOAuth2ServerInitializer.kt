package no.nav.familie.ba.infotrygd.testutil

import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.support.TestPropertySourceUtils
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.wait.strategy.Wait

class MockOAuth2ServerInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
            applicationContext,
            "AZURE_OPENID_CONFIG_ISSUER=$issuerUrl",
            "AUTHORIZATION_URL=$issuerUrl/authorize",
            "TOKEN_URL=$issuerUrl/token",
        )
    }

    companion object {
        private const val SERVER_PORT = 6969

        private val mockOAuth2Server: GenericContainer<*> by lazy {
            GenericContainer("ghcr.io/navikt/mock-oauth2-server:3.0.1")
                .withNetwork(Network.newNetwork())
                .withNetworkAliases("azuread")
                .withExposedPorts(SERVER_PORT)
                .withEnv(
                    mapOf(
                        "SERVER_PORT" to SERVER_PORT.toString(),
                        "TZ" to "Europe/Oslo",
                    ),
                ).waitingFor(Wait.forHttp("/default/.well-known/openid-configuration").forStatusCode(200))
                .apply { start() }
        }

        val issuerUrl: String
            get() {
                val port = mockOAuth2Server.getMappedPort(SERVER_PORT)
                return "http://localhost:$port/default"
            }
    }
}
