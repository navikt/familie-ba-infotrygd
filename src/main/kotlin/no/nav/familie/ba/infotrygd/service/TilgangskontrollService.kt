package no.nav.familie.ba.infotrygd.service
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class TilgangskontrollService(
    private val tokenValidationContextHolder: TokenValidationContextHolder,
    @Value("\${TEAMFAMILIE_FORVALTNING_GROUP_ID}") private val forvalterGroupId: String
) {

    fun sjekkTilgang() {
        val roles = tokenValidationContextHolder.tokenValidationContext.anyValidClaims.map {
            it.getAsList("roles")
        }.orElse(emptyList())
        val groups = tokenValidationContextHolder.tokenValidationContext.anyValidClaims.map {
            it.getAsList("groups")
        }.orElse(emptyList())

        if (!roles.contains(ACCESS_AS_APPLICATION_ROLE) && !groups.contains(forvalterGroupId)) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "User har ikke tilgang til å kalle tjenesten!")
        }
    }

    companion object {
        const val ACCESS_AS_APPLICATION_ROLE = "access_as_application"
    }
}