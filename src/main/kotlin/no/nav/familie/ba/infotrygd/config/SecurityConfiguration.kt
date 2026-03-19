package no.nav.familie.ba.infotrygd.config

import no.nav.familie.ba.infotrygd.security.AzureJwtAuthenticationConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer.withDefaults
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfiguration(
	private val azureJwtAuthenticationConverter: AzureJwtAuthenticationConverter,
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
					"/testtoken/**"
				).permitAll()
				it.anyRequest().authenticated()
			}
			.oauth2ResourceServer {
				it.jwt { jwt ->
					jwt.jwtAuthenticationConverter(azureJwtAuthenticationConverter)
				}
			}
			.httpBasic(withDefaults())
			.build()
	}
}
