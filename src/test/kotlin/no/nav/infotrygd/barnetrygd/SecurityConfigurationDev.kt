package no.nav.infotrygd.barnetrygd

import no.nav.security.oidc.test.support.spring.TokenGeneratorConfiguration
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Profile

@Configuration
@Profile("test")
@Import(TokenGeneratorConfiguration::class)
class SecurityConfigurationDev
