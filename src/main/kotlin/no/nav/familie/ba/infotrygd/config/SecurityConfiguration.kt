package no.nav.familie.ba.infotrygd.config

import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.context.annotation.Configuration

@EnableJwtTokenValidation(ignore = ["org.springframework", "springfox", "org.springdoc"])
@Configuration
class SecurityConfiguration
