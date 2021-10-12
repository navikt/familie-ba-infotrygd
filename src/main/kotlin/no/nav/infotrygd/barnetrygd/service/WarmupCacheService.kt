package no.nav.infotrygd.barnetrygd.service

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Year

@Component
class WarmupCacheService(val barnetrygdService: BarnetrygdService) {

    private val logger = LoggerFactory.getLogger(WarmupCacheService::class.java)

    @Scheduled(fixedDelay = 24*60*60*1000, initialDelay = 5*60*1000)
    fun warmupSkatteetatenPersonerCache() {
        val inneværendeÅr = Year.now()

        logger.info("Henter personer med utvidet barnetrygd for å oppdatere cache")
        barnetrygdService.finnPersonerMedUtvidetBarnetrygd(inneværendeÅr.plusYears(1).toString())
        barnetrygdService.finnPersonerMedUtvidetBarnetrygd(inneværendeÅr.toString())
        barnetrygdService.finnPersonerMedUtvidetBarnetrygd(inneværendeÅr.minusYears(1).toString())
    }
}