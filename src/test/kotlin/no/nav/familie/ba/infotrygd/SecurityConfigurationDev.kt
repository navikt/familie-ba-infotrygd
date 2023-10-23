package no.nav.familie.ba.infotrygd

import no.nav.security.token.support.test.spring.TokenGeneratorConfiguration
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Profile

@Configuration
@Profile("test")
@Import(TokenGeneratorConfiguration::class)
class SecurityConfigurationDev
