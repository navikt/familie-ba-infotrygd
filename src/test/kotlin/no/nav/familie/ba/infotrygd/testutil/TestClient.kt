package no.nav.familie.ba.infotrygd.testutil

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.restclient.RestTemplateBuilder
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpRequest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.ResponseErrorHandler
import org.springframework.web.client.RestTemplate
import org.springframework.web.server.ResponseStatusException
import java.net.URI

@Profile("test")
@Component
class TestClient(
    private val restTemplateBuilder: RestTemplateBuilder,
    @param:Value("\${AZURE_OPENID_CONFIG_ISSUER}") private val azureIssuer: String,
) {

    fun restTemplate(port: Int, subject: String = "12345678910"): RestTemplate {
        return restTemplateBuilder(port)
            .additionalInterceptors(MockOAuth2ServerAccessTokenInterceptor(subject))
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
            val body = response.body.bufferedReader().use { it.readText() }

            throw ResponseStatusException(
                status,
                "Feil ved REST-kall: ${status.value()} $method - $url\n$body"
            )
        }
    }

    private inner class MockOAuth2ServerAccessTokenInterceptor(
        private val subject: String,
    ) : ClientHttpRequestInterceptor {

        override fun intercept(
            request: HttpRequest,
            body: ByteArray,
            execution: ClientHttpRequestExecution
        ): ClientHttpResponse {
            val token = hentToken(subject)
            request.headers.setBearerAuth(token)
            return execution.execute(request, body)
        }

        private fun hentToken(subject: String): String {
            val tokenEndpoint = "$azureIssuer/token"
            val headers = HttpHeaders().apply {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
            }
            val form = LinkedMultiValueMap<String, String>().apply {
                add("grant_type", "client_credentials")
                add("client_id", "theclientid")
                add("client_secret", "secret")
                add("subject", subject)
                add("audience", "default")
            }

            val response = RestTemplate().postForObject(tokenEndpoint, HttpEntity(form, headers), Map::class.java)
            return response?.get("access_token") as? String
                ?: throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Mangler access_token fra mock-oauth2-server")
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