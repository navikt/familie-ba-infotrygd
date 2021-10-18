package no.nav.familie.ba.infotrygd.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import javax.validation.constraints.NotEmpty

@Validated
@Component
@ConfigurationProperties(prefix = "spring.datasource")
class ValidateConfig {

    @NotEmpty
    lateinit var password: String

    @NotEmpty
    lateinit var username: String
}