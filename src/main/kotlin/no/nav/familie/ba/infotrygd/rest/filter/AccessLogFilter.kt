package no.nav.familie.ba.infotrygd.rest.filter

import org.slf4j.LoggerFactory
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpFilter
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse


class AccessLogFilter(private val applicationName: String) : HttpFilter() {
    private val log = LoggerFactory.getLogger(javaClass)

    private val dontLog = setOf("/actuator/health", "/actuator/prometheus")

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val req = request as HttpServletRequest
        val res = response as HttpServletResponse

        val millis = time { chain.doFilter(request, response) }

        if(!dontLog.contains(req.requestURI)) {
            log.info("[${millis}ms]\t${res.status} ${req.method} ${req.requestURI}")
        }
    }

    private fun time(block: () -> Unit): Long {
        val t0 = System.nanoTime()
        block()
        val t1 = System.nanoTime()
        return (t1 - t0) / 1_000_000
    }

    override fun toString(): String {
        return "${javaClass.simpleName} [applicationName=$applicationName]"
    }

}