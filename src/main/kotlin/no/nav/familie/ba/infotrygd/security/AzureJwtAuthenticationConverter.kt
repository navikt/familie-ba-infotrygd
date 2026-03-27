package no.nav.familie.ba.infotrygd.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component

@Component
@Profile("!test")
class AzureJwtAuthenticationConverter(
    @param:Value("\${TEAMFAMILIE_FORVALTNING_GROUP_ID}") protected val forvalterGroupId: String,
    @param:Value("\${TEAMFAMILIE_SAKSBEHANDLER_GROUP_ID}") protected val saksbehandlerGroupId: String,
    @param:Value("\${TEAMFAMILIE_VEILEDER_GROUP_ID}") protected val veilederGroupId: String,
    @param:Value("\${TEAMFAMILIE_BESLUTTER_GROUP_ID}") protected val beslutterGroupId: String,
) : Converter<Jwt, AbstractAuthenticationToken> {

    override fun convert(jwt: Jwt): AbstractAuthenticationToken {
        val groups = jwt.getClaimAsStringList("groups") ?: emptyList()
        val roles = jwt.getClaimAsStringList("roles") ?: emptyList()

        val roller = buildSet {
            if (groups.contains(forvalterGroupId)) add(Rolle.FORVALTER)
            if (groups.contains(saksbehandlerGroupId)) add(Rolle.SAKSBEHANDLER)
            if (groups.contains(veilederGroupId)) add(Rolle.VEILEDER)
            if (groups.contains(beslutterGroupId)) add(Rolle.BESLUTTER)
            if (roles.contains(ACCESS_AS_APPLICATION_ROLE)) add(Rolle.APPLICATION)
        }

        val authorities = roller.map { SimpleGrantedAuthority(it.authority()) }
        return JwtAuthenticationToken(jwt, authorities)
    }

    companion object {
        private const val ACCESS_AS_APPLICATION_ROLE = "access_as_application"
    }
}
