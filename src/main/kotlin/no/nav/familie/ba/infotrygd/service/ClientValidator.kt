package no.nav.familie.ba.infotrygd.service

import no.nav.familie.ba.infotrygd.Profiles
import no.nav.security.token.support.core.context.TokenValidationContext
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.env.Environment
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException


@Component
class ClientValidator(
    private val environment: Environment,
    private val ctxHolder: TokenValidationContextHolder?,

    @Value("\${rolle.teamfamilie.forvalter}")
    private val forvalterRolleTeamfamilie: String,
    @Value("\${no.nav.security.jwt.issuer.azure.accepted_audience}")
    private val audience: String,
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
            } else if (azureClientIds.contains("$AzureIssuer/$audience")) { // true for tokens opprettet via OpenAPI-flyt
                return azureGroups().any { it.contains(forvalterRolleTeamfamilie) }
            }
        }

        return false
    }

    private fun issuerSubjects(): List<String> {
        val oidcValidationContext: TokenValidationContext = ctxHolder?.tokenValidationContext
            ?: return emptyList()

        return oidcValidationContext.issuers.map {
            val subject = oidcValidationContext.getClaims(it).subject
            "$it/$subject"
        }
    }

    private fun azureClientIds() : List<String> {
        val oidcValidationContext: TokenValidationContext = ctxHolder?.tokenValidationContext
            ?: return emptyList()

        return oidcValidationContext.issuers
            .filter { it == AzureIssuer }
            .map { oidcValidationContext.getClaims(it) }
            .filterNotNull()
            .filter { it.get(AzureV2ClientIdClaim) != null || it.get(AzureV1ClientIdClaim) != null }
            .map { "${AzureIssuer}/${it.get(AzureV2ClientIdClaim)?:it.get(AzureV1ClientIdClaim)!!}" }
    }

    private fun azureGroups(): List<String> {
        val oidcValidationContext: TokenValidationContext = ctxHolder?.tokenValidationContext
            ?: return emptyList()

        return oidcValidationContext.issuers
            .filter { it == AzureIssuer }
            .map { oidcValidationContext.getClaims(it) }
            .filterNotNull()
            .map { "${it.get("groups")}" }
    }

    private companion object {
        private const val AzureIssuer = "azure"
        // https://docs.microsoft.com/en-us/azure/active-directory/develop/access-tokens#payload-claims
        private const val AzureV1ClientIdClaim = "appid"
        private const val AzureV2ClientIdClaim = "azp"
    }
}