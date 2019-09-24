package no.nav.infotrygd.beregningsgrunnlag
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

class LogFilter : Filter<ILoggingEvent>() {
    // Denne er her for å fikse problem med testcontainers<->IntelliJ
    override fun decide(event: ILoggingEvent?): FilterReply {
        if(event == null) {
            return FilterReply.NEUTRAL
        }

        if(event.loggerName.contains('[')) {
            return FilterReply.DENY
        }
        return FilterReply.NEUTRAL
    }
}