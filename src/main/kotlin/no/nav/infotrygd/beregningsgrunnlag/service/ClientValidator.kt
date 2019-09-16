package no.nav.infotrygd.beregningsgrunnlag.service

import no.nav.infotrygd.beregningsgrunnlag.Profiles
import no.nav.security.oidc.context.OIDCClaims
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

    @Value("\${app.security.issuer}")
    private val issuer: String,

    @Value("\${app.security.clientWhitelist}")
    clientWhitelistStr: String
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val clientWhitelist = clientWhitelistStr.split(',').toSet()

    fun authorizeClient() {
        if(!authorized()) {
            val msg = "Klienten er ikke autorisert: ${subject()}"
            logger.info(msg)
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, msg)
        }
    }

    private fun authorized(): Boolean {
        if(environment.acceptsProfiles(Profiles.NOAUTH)) {
            return true
        }
        return clientWhitelist.contains(subject())
    }

    private fun subject(): String? {
        val oidcValidationContext: OIDCValidationContext? = ctxHolder?.oidcValidationContext
        val claims: OIDCClaims? = oidcValidationContext?.getClaims(issuer)
        return claims?.subject
    }
}