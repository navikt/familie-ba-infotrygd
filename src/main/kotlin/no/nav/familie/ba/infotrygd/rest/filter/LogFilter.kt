package no.nav.familie.ba.infotrygd.rest.filter

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.Ordered.LOWEST_PRECEDENCE
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.GenericFilterBean
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@Component
@Order(LOWEST_PRECEDENCE)
class LogFilter(@Value("\${spring.application.name}") private val applicationName: String) : GenericFilterBean() {
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