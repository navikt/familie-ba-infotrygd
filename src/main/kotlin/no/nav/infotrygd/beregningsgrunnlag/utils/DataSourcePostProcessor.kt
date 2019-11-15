package no.nav.infotrygd.beregningsgrunnlag.utils

import net.ttddyy.dsproxy.listener.logging.DefaultQueryLogEntryCreator
import net.ttddyy.dsproxy.listener.logging.SLF4JLogLevel
import net.ttddyy.dsproxy.listener.logging.SLF4JQueryLoggingListener
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder
import no.nav.infotrygd.beregningsgrunnlag.Profiles.DEBUG_SQL
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import javax.sql.DataSource

@Component
@Profile(DEBUG_SQL)
class DataSourcePostProcessor : BeanPostProcessor {
    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        if(bean is DataSource) {
            return createProxy(bean)
        }
        return bean
    }

    fun createProxy(original: DataSource): DataSource {
        val loggingListener = SLF4JQueryLoggingListener()
        loggingListener.logLevel = SLF4JLogLevel.INFO
        loggingListener.queryLogEntryCreator = DefaultQueryLogEntryCreator()
        return ProxyDataSourceBuilder
            .create(original)
            .name("DataSourceProxy")
            .listener(loggingListener)
            .build()
    }
}