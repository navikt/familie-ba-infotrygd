package no.nav.familie.ba.infotrygd.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component

@Primary
@Profile("test")
@Component
class TestAzureJwtAuthenticationConverter(
    @param:Value("\${TEAMFAMILIE_FORVALTNING_GROUP_ID}") forvalterGroupId: String,
    @param:Value("\${TEAMFAMILIE_SAKSBEHANDLER_GROUP_ID}") saksbehandlerGroupId: String,
    @param:Value("\${TEAMFAMILIE_VEILEDER_GROUP_ID}") veilederGroupId: String,
    @param:Value("\${TEAMFAMILIE_BESLUTTER_GROUP_ID}") beslutterGroupId: String,
    @param:Value("\${TEST_APPLICATION_SUBJECT:}") private val testApplicationSubject: String,
) : Converter<Jwt, AbstractAuthenticationToken>  {

    override fun convert(jwt: Jwt): AbstractAuthenticationToken {
        val roller = buildSet {
            if (testApplicationSubject.isNotBlank() && jwt.subject == testApplicationSubject) add(Rolle.APPLICATION)
        }

        val authorities = roller.map { SimpleGrantedAuthority(it.authority()) }
        return JwtAuthenticationToken(jwt, authorities)
    }
}
