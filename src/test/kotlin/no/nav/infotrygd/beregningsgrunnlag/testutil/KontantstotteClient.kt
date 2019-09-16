package no.nav.infotrygd.beregningsgrunnlag.testutil

import org.springframework.web.reactive.function.client.WebClient

fun svangerskapspengerClient(port: Int): WebClient {
    val token = authToken(port)
    return WebClient.builder()
        .baseUrl(baseUrl(port))
        .defaultHeader("Authorization", "Bearer $token")
        .build()
}

fun svangerskapspengerNoAuthClient(port: Int): WebClient {
    return WebClient.builder()
        .baseUrl("http://localhost:$port")
        .build()
}

fun authToken(port: Int): String {
    val url = baseUrl(port)
    return WebClient.create("$url/local/jwt").get()
        .retrieve()
        .bodyToMono(String::class.java)
        .block() !!
}

private fun baseUrl(port: Int) = "http://localhost:$port"