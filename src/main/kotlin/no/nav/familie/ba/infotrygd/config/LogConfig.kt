package no.nav.familie.ba.infotrygd.config

import no.nav.familie.ba.infotrygd.rest.filter.AccessLogFilter
import no.nav.familie.log.filter.LogFilter
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class  LogConfig {

    @Bean
    fun logFilterFelles(): FilterRegistrationBean<LogFilter> {
        val filterRegistration: FilterRegistrationBean<LogFilter> = FilterRegistrationBean()
        filterRegistration.filter = LogFilter()
        filterRegistration.order = 1
        return filterRegistration
    }

    @Bean
    fun accessLogFilter(): FilterRegistrationBean<AccessLogFilter> {
        val filterRegistration: FilterRegistrationBean<AccessLogFilter> = FilterRegistrationBean()
        filterRegistration.filter = AccessLogFilter("familie-ba-infotrygd")
        filterRegistration.order = 10000
        return filterRegistration
    }
}