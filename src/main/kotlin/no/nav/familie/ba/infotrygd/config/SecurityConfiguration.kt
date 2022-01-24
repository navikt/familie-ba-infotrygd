package no.nav.familie.ba.infotrygd.config

import no.nav.familie.ba.infotrygd.Profiles
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@EnableJwtTokenValidation(ignore = ["org.springframework", "springfox", "org.springdoc"])
@Profile("!${Profiles.NOAUTH}")
@Configuration
class SecurityConfiguration
