package no.nav.familie.ba.infotrygd.testutil

import com.nimbusds.jose.JOSEObjectType
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback
import org.springframework.boot.restclient.RestTemplateBuilder
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpMethod
import org.springframework.http.HttpRequest
import org.springframework.http.HttpStatus
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.client.ResponseErrorHandler
import org.springframework.web.client.RestTemplate
import org.springframework.web.server.ResponseStatusException
import java.net.URI

@Profile("test")
@Component
class TestClient(
    private val restTemplateBuilder: RestTemplateBuilder,
    private val server: MockOAuth2Server
) {

    fun restTemplate(port: Int, subject: String = "12345678910"): RestTemplate {
        val grupper = listOf("gruppe-123")
        val roller = listOf("access_as_application")
        return restTemplateBuilder(port)
            .additionalInterceptors(MockOAuth2ServerAccessTokenInterceptor(grupper, roller, subject))
            .build()
    }

    fun restTemplateNoAuth(port: Int) = restTemplateBuilder(port).build()

    private fun baseUrl(port: Int) = "http://localhost:$port"

    private fun restTemplateBuilder(port: Int) = restTemplateBuilder
        .errorHandler(TestErrorHandler())
        .rootUri(baseUrl(port))
        .interceptors(ExceptionHandlerInterceptor())

    private inner class TestErrorHandler : ResponseErrorHandler {

        override fun hasError(response: ClientHttpResponse): Boolean {
            val rawStatusCode = response.statusCode.value()
            val series = HttpStatus.Series.resolve(rawStatusCode)
            return series == HttpStatus.Series.CLIENT_ERROR || series == HttpStatus.Series.SERVER_ERROR
        }

        override fun handleError(url: URI, method: HttpMethod, response: ClientHttpResponse) {
            val status = response.statusCode
            val contentType = response.headers.contentType

            val body = response.body.bufferedReader().use { it.readText() }

            throw ResponseStatusException(
                status,
                "Feil ved REST-kall: ${status.value()} $method - $url\n$body"
            )
        }
    }

    private inner class MockOAuth2ServerAccessTokenInterceptor(
        private val grupper: List<String>,
        private val roller: List<String>,
        val sub: String
    ) :
        ClientHttpRequestInterceptor {

        override fun intercept(
            request: HttpRequest,
            body: ByteArray,
            execution: ClientHttpRequestExecution
        ): ClientHttpResponse {
            val token = server.issueToken(
                issuerId = "default",
                "theclientid",
                DefaultOAuth2TokenCallback(
                    issuerId = "azuread",
                    subject = sub,
                    audience = listOf("default"),
                    typeHeader = JOSEObjectType.JWT.type,
                    claims = mapOf("groups" to grupper, "roles" to roller)
                )
            )
            request.headers.setBearerAuth(token.serialize())
            return execution.execute(request, body)
        }
    }
}

class ExceptionHandlerInterceptor : ClientHttpRequestInterceptor {
    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse {
        try {
            return execution.execute(request, body)
        } catch (e: Exception) {
            val feilmelding =
                "Feil ved REST-kall ${request.method ?: ""} ${request.uri.scheme ?: ""}://${request.uri.host ?: ""}" +
                        ":${request.uri.port}${request.uri.path ?: ""}\n" +
                        "${e.message}"
            throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                feilmelding,
                e.cause
            )
        }
    }
}