package no.nav.familie.ba.infotrygd.config

import no.nav.familie.ba.infotrygd.rest.filter.AccessLogFilter
import no.nav.familie.log.NavSystemtype
import no.nav.familie.log.filter.LogFilter
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered.LOWEST_PRECEDENCE

@Configuration
class LogConfig {
    @Bean
    fun logFilterFelles(): FilterRegistrationBean<LogFilter> {
        val filterRegistration: FilterRegistrationBean<LogFilter> = FilterRegistrationBean()
        filterRegistration.setFilter(LogFilter(NavSystemtype.NAV_INTEGRASJON))
        filterRegistration.order = 1
        return filterRegistration
    }

    @Bean
    fun accessLogFilter(): FilterRegistrationBean<AccessLogFilter> {
        val filterRegistration: FilterRegistrationBean<AccessLogFilter> = FilterRegistrationBean()
        filterRegistration.setFilter(AccessLogFilter("familie-ba-infotrygd"))
        filterRegistration.order = LOWEST_PRECEDENCE
        return filterRegistration
    }
}
