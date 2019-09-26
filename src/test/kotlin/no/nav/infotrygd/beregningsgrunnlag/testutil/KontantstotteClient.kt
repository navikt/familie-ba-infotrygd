package no.nav.infotrygd.beregningsgrunnlag.testutil

import org.springframework.web.reactive.function.client.WebClient

fun restClient(port: Int, subject: String? = null): WebClient {
    val token = authToken(port, subject)
    return WebClient.builder()
        .baseUrl(baseUrl(port))
        .defaultHeader("Authorization", "Bearer $token")
        .build()
}

fun restClientNoAuth(port: Int): WebClient {
    return WebClient.builder()
        .baseUrl("http://localhost:$port")
        .build()
}

fun authToken(port: Int, subject: String? = null): String {
    val url = baseUrl(port)
    val subjectQuery = subject?.let { "?subject=$it" } ?: ""
    return WebClient.create("$url/local/jwt$subjectQuery").get()
        .retrieve()
        .bodyToMono(String::class.java)
        .block() !!
}

private fun baseUrl(port: Int) = "http://localhost:$port"