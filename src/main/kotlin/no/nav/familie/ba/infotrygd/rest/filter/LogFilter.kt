package no.nav.familie.ba.infotrygd.rest.filter

import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.Ordered.LOWEST_PRECEDENCE
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.GenericFilterBean
import java.util.*
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@Component
@Order(LOWEST_PRECEDENCE)
class LogFilter(@Value("\${spring.application.name}") private val applicationName: String) : GenericFilterBean() {
    private val log = LoggerFactory.getLogger(javaClass)
    private val consumerIdHeader = "Nav-Consumer-Id"
    private val callIdHeader = "Nav-CallId"

    private val dontLog = setOf("/actuator/health", "/actuator/prometheus")

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        putValues(HttpServletRequest::class.java.cast(request))
        val req = request as HttpServletRequest
        val res = response as HttpServletResponse
        try {
            val millis = time { chain.doFilter(request, response) }

            if(!dontLog.contains(req.requestURI)) {
                log.info("[${millis}ms]\t${res.status} ${req.method} ${req.requestURI}")
            }

        } finally {
            MDC.clear()
        }
    }

    private fun time(block: () -> Unit): Long {
        val t0 = System.nanoTime()
        block()
        val t1 = System.nanoTime()
        return (t1 - t0) / 1_000_000
    }

    private fun putValues(request: HttpServletRequest) {
        try {
            toMDC(consumerIdHeader, request.getHeader(consumerIdHeader) ?: applicationName)
            toMDC(callIdHeader, request.getHeader(callIdHeader) ?: UUID.randomUUID().toString())
        } catch (e: Exception) {
            log.warn("Noe gikk galt ved setting av MDC-verdier for request {}, MDC-verdier er inkomplette", request.requestURI, e)
        }
    }

    override fun toString(): String {
        return "${javaClass.simpleName} [applicationName=$applicationName]"
    }

    fun toMDC(key: String, value: String) {
        MDC.put(key, value)
    }
}