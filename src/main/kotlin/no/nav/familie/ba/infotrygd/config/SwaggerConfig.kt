package no.nav.familie.ba.infotrygd.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.OAuthFlow
import io.swagger.v3.oas.models.security.OAuthFlows
import io.swagger.v3.oas.models.security.Scopes
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig(@Value("\${AUTHORIZATION_URL}")
                    val authorizationUrl: String,
                    @Value("\${AZURE_OPENID_CONFIG_TOKEN_ENDPOINT}")
                    val tokenUrl: String,
                    @Value("\${API_SCOPE}")
                    val apiScope: String) {

    @Bean
    fun openApi(): OpenAPI {
        return OpenAPI().info(Info().title("BA Infotrygd API"))
            .components(Components()
                            .addSecuritySchemes("oauth2", oauth2SecurityScheme())
                            .addSecuritySchemes("bearer", bearerTokenSecurityScheme()))
            .addSecurityItem(SecurityRequirement().addList("oauth2", listOf("read", "write")))
            .addSecurityItem(SecurityRequirement().addList("bearer", listOf("read", "write")))
    }

    private fun oauth2SecurityScheme(): SecurityScheme {
        return SecurityScheme()
            .name("oauth2")
            .type(SecurityScheme.Type.OAUTH2)
            .scheme("oauth2")
            .`in`(SecurityScheme.In.HEADER)
            .flows(OAuthFlows()
                       .authorizationCode(OAuthFlow().authorizationUrl(authorizationUrl)
                                              .tokenUrl(tokenUrl)
                                              .scopes(Scopes().addString(apiScope, "read,write"))))

    }

    private fun bearerTokenSecurityScheme(): SecurityScheme {
        return SecurityScheme()
            .type(SecurityScheme.Type.APIKEY)
            .scheme("bearer")
            .bearerFormat("JWT")
            .`in`(SecurityScheme.In.HEADER)
            .name("Authorization")
    }

}