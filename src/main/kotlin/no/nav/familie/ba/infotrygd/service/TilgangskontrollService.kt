package no.nav.familie.ba.infotrygd.service
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class TilgangskontrollService(
    private val tokenValidationContextHolder: TokenValidationContextHolder,
    @Value("\${TEAMFAMILIE_VEILEDER_GROUP_ID}") private val veilederGroupId: String,
    @Value("\${TEAMFAMILIE_SAKSBEHANDLER_GROUP_ID}") private val saksbehandlerGroupId: String,
    @Value("\${TEAMFAMILIE_BESLUTTER_GROUP_ID}") private val beslutterGroupId: String,
    @Value("\${TEAMFAMILIE_FORVALTNING_GROUP_ID}") private val forvalterGroupId: String
) {
    val secureLogger = LoggerFactory.getLogger("secureLogger")

    fun sjekkTilgang() {
        val roles = tokenValidationContextHolder.getTokenValidationContext().anyValidClaims?.getAsList("roles")
            ?: emptyList()
        val groups = tokenValidationContextHolder.getTokenValidationContext().anyValidClaims?.getAsList("groups")
            ?: emptyList()

        secureLogger.info("Roller: $roles")
        secureLogger.info("Grupper: $groups")
        if (!(roles.contains(ACCESS_AS_APPLICATION_ROLE) ||
                    groups.contains(veilederGroupId) ||
                    groups.contains(saksbehandlerGroupId) ||
                    groups.contains(beslutterGroupId) ||
                    groups.contains(forvalterGroupId))
        ) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Bruker har ikke tilgang til å kalle tjenesten!")
        }
    }

    companion object {
        const val ACCESS_AS_APPLICATION_ROLE = "access_as_application"
    }
}