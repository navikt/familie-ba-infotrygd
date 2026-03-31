package no.nav.familie.ba.infotrygd.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.web.SecurityFilterChain


@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfiguration(
	private val azureJwtAuthenticationConverter: Converter<Jwt, AbstractAuthenticationToken>
) {

	@Bean
	fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
		return http
			.csrf { it.disable() }
			.authorizeHttpRequests {
				it.requestMatchers(
					"/internal/**",
					"/actuator/**",
					"/swagger-ui/**",
					"/swagger-ui.html",
					"/v3/api-docs/**",
				).permitAll()
				it.anyRequest().authenticated()
			}
			.oauth2ResourceServer {
				it.jwt { jwt ->
					jwt.jwtAuthenticationConverter(azureJwtAuthenticationConverter)
				}
			}
			.httpBasic { it.disable() }
			.build()
	}
}
