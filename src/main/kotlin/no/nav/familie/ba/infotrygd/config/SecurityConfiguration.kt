package no.nav.familie.ba.infotrygd.config

import no.nav.familie.ba.infotrygd.Profiles
import no.nav.security.token.support.spring.SpringTokenValidationContextHolder
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@EnableJwtTokenValidation(ignore = ["org.springframework", "springfox", "org.springdoc"])
@Profile("!${Profiles.NOAUTH}")
@Configuration
class SecurityConfiguration(
    @Value("\${rolle.teamfamilie.forvalter}")
    val forvalterRolleTeamfamilie: String
) {

    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    @Bean
    fun requestFilter() = object : OncePerRequestFilter() {
        override fun doFilterInternal(
            request: HttpServletRequest,
            response: HttpServletResponse,
            filterChain: FilterChain
        ) {
            val grupper = hentGrupper()
            if (!grupper.contains(forvalterRolleTeamfamilie) && !environment.activeProfiles.contains("test")) {
                secureLogger.info("Ugyldig rolle for url=${request.requestURI} grupper=$grupper, forvalterRolleTeamfamilie=$forvalterRolleTeamfamilie")
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Handling krever teamfamilie forvalter rolle")
            } else {
                try {
                    filterChain.doFilter(request, response)
                } catch (e: Exception) {
                    secureLogger.warn("Uventet feil i doFilter for url=${request.requestURI} grupper=$grupper", e)
                    throw e
                }
            }
        }

        override fun shouldNotFilter(request: HttpServletRequest): Boolean {
            return request.requestURI.contains("/internal") ||
                    request.requestURI.contains("/actuator") ||
                    request.requestURI.startsWith("/swagger") ||
                    request.requestURI.startsWith("/v3")
        } // i bruk av swagger
    }

    private fun hentGrupper(): List<String> {
        return Result.runCatching { SpringTokenValidationContextHolder().tokenValidationContext }
            .fold(
                onSuccess = {
                    @Suppress("UNCHECKED_CAST")
                    it?.getClaims("azure")?.get("groups") as List<String>? ?: emptyList()
                },
                onFailure = { emptyList() }
            )
    }
}
