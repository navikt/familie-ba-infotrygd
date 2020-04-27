package no.nav.infotrygd.beregningsgrunnlag.service

import no.nav.infotrygd.beregningsgrunnlag.Profiles
import no.nav.security.oidc.context.OIDCRequestContextHolder
import no.nav.security.oidc.context.OIDCValidationContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.env.Environment
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException

@Component
class ClientValidator(
    private val environment: Environment,
    private val ctxHolder: OIDCRequestContextHolder?,

    @Value("\${app.security.clientWhitelist}")
    clientWhitelistStr: String
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val clientWhitelist = clientWhitelistStr.split(',').toSet()

    fun authorizeClient() {
        if(!authorized()) {
            val msg = "Klienten er ikke autorisert: ${issuerSubjects().plus(azureClientIds())}"
            logger.info(msg)
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, msg)
        }
    }

    private fun authorized(): Boolean {
        if(environment.acceptsProfiles(Profiles.NOAUTH)) {
            return true
        }

        val subjects = issuerSubjects()
        for(entry in clientWhitelist) {
            if(subjects.contains(entry)) {
                return true
            }
        }

        val azureClientIds = azureClientIds()
        for (entry in clientWhitelist) {
            if (azureClientIds.contains(entry)) {
                return true
            }
        }

        return false
    }

    private fun issuerSubjects(): List<String> {
        val oidcValidationContext: OIDCValidationContext? = ctxHolder?.oidcValidationContext
        val allClaims = oidcValidationContext?.allClaims ?: return emptyList()
        return allClaims
            .filter { it.value.subject != null }
            .map { "${it.key}/${it.value.subject!!}" }
    }

    private fun azureClientIds() : List<String> {
        val oidcValidationContext: OIDCValidationContext? = ctxHolder?.oidcValidationContext
        val allClaims = oidcValidationContext?.allClaims ?: return emptyList()
        return allClaims
            .filter { (issuer, _) -> AzureIssuer == issuer }
            .filter { (_, claims) -> claims.get(AzureV2ClientIdClaim) != null || claims.get(AzureV1ClientIdClaim) != null }
            .map { (_, claims) -> "${AzureIssuer}/${claims.get(AzureV2ClientIdClaim)?:claims.get(AzureV1ClientIdClaim)!!}" }
    }

    private companion object {
        private const val AzureIssuer = "azure"
        // https://docs.microsoft.com/en-us/azure/active-directory/develop/access-tokens#payload-claims
        private const val AzureV1ClientIdClaim = "appid"
        private const val AzureV2ClientIdClaim = "azp"
    }
}