package no.nav.infotrygd.beregningsgrunnlag.service

import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.PlainJWT
import no.nav.infotrygd.beregningsgrunnlag.Profiles
import no.nav.security.oidc.context.OIDCClaims
import no.nav.security.oidc.context.OIDCRequestContextHolder
import no.nav.security.oidc.context.OIDCValidationContext
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
    lateinit var oidcRequestContextHolder: OIDCRequestContextHolder

    @MockBean
    lateinit var oidcValidationContext: OIDCValidationContext

    @Autowired
    lateinit var clientValidator : ClientValidator

    private fun setupMocks(issuer: String, claimKey: String, claimValue: String) {
        `when`(environment.acceptsProfiles(Profiles.NOAUTH)).thenReturn(false)

        `when`(oidcValidationContext.allClaims).thenReturn(mapOf(
            issuer to OIDCClaims(PlainJWT(JWTClaimsSet.parse("""
                {
                    "$claimKey": "$claimValue"
                }
            """.trimIndent())))
        ))

        `when`(oidcRequestContextHolder.oidcValidationContext).thenReturn(oidcValidationContext)
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