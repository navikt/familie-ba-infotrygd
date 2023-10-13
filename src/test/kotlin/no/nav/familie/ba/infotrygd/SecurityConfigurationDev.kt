package no.nav.familie.ba.infotrygd

import no.nav.security.token.support.spring.test.MockLoginController
import no.nav.security.token.support.spring.test.MockOAuth2ServerAutoConfiguration
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Profile

@Configuration
@Profile("test")
@Import(MockOAuth2ServerAutoConfiguration::class, MockLoginController::class)
class SecurityConfigurationDev
