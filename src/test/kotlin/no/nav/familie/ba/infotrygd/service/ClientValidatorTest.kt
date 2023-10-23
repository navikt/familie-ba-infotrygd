package no.nav.familie.ba.infotrygd.service

import com.nimbusds.jwt.JWTClaimsSet
import no.nav.familie.ba.infotrygd.Profiles
import no.nav.security.token.support.core.context.TokenValidationContext
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.security.token.support.core.jwt.JwtTokenClaims
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.core.env.Environment
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import org.mockito.Mockito.`when`
import org.springframework.web.server.ResponseStatusException


@RunWith(SpringRunner::class)
@ContextConfiguration(classes = [ClientValidator::class])
@TestPropertySource(properties = [
    "app.security.clientWhitelist=sts/sub-claim,azure/sub-claim,azure/azp-claim,azure/appid-claim"
])
internal class ClientValidatorTest {

    @MockBean
    private lateinit var environment: Environment

    @MockBean
    lateinit var oidcRequestContextHolder: TokenValidationContextHolder

    @MockBean
    lateinit var validationContext: TokenValidationContext

    @Autowired
    lateinit var clientValidator : ClientValidator

    private fun setupMocks(issuer: String, claimKey: String, claimValue: String) {
        `when`(environment.acceptsProfiles(Profiles.NOAUTH)).thenReturn(false)

        `when`(validationContext.issuers).thenReturn(listOf(issuer))

        val claims = JwtTokenClaims(
            JWTClaimsSet.Builder()
                .issuer(issuer)
                .claim(claimKey, claimValue)
                .build())

        `when`(validationContext.getClaims(issuer)).thenReturn(claims)

        `when`(oidcRequestContextHolder.tokenValidationContext).thenReturn(validationContext)
    }

    @Test
    fun `Autorisert STS token`() {
        setupMocks("sts", "sub", "sub-claim")
        clientValidator.authorizeClient()
    }

    @Test(expected = ResponseStatusException::class)
    fun `Uautorisert STS token`() {
        setupMocks("sts", "sub", "ikke-whitelisted")
        clientValidator.authorizeClient()
    }

    @Test
    fun `Autorisert Azure token (subject)`() {
        setupMocks("azure", "sub", "sub-claim")
        clientValidator.authorizeClient()
    }

    @Test(expected = ResponseStatusException::class)
    fun `Uautorisert Azure token (subject)`() {
        setupMocks("azure", "sub", "ikke-whitelisted")
        clientValidator.authorizeClient()
    }

    @Test
    fun `Autorisert AzureV1 token (clientId)`() {
        setupMocks("azure", "appid", "appid-claim")
        clientValidator.authorizeClient()
    }

    @Test(expected = ResponseStatusException::class)
    fun `Uautorisert AzureV1 token (clientId)`() {
        setupMocks("azure", "appid", "ikke-whitelisted")
        clientValidator.authorizeClient()
    }

    @Test
    fun `Autorisert AzureV2 token (clientId)`() {
        setupMocks("azure", "azp", "azp-claim")
        clientValidator.authorizeClient()
    }

    @Test(expected = ResponseStatusException::class)
    fun `Uautorisert AzureV2 token (clientId)`() {
        setupMocks("azure", "azp", "ikke-whitelisted")
        clientValidator.authorizeClient()
    }

    @Test(expected = ResponseStatusException::class)
    fun `Uautorisert Issuer`() {
        setupMocks("annen-issuer", "sub", "sub-claim")
        clientValidator.authorizeClient()
    }
}