package no.nav.familie.ba.infotrygd.service

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Year

@Component
class WarmupCacheService(val barnetrygdService: BarnetrygdService) {

    private val logger = LoggerFactory.getLogger(WarmupCacheService::class.java)

    @Scheduled(fixedDelay = 24 * 60 * 60 * 1000, initialDelay = 3 * 60 * 1000)
    fun warmupSkatteetatenPersonerCache() {
        val inneværendeÅr = Year.now()

        logger.info("Henter personer med utvidet barnetrygd for å oppdatere cache ${inneværendeÅr.plusYears(1)}")
        val nextYearResponse = barnetrygdService.finnPersonerUtvidetBarnetrygdSkatt(inneværendeÅr.plusYears(1).toString())
        logger.info("Henter personer med utvidet barnetrygd for å oppdatere cache $inneværendeÅr")
        val inneværendeÅrResponse = barnetrygdService.finnPersonerUtvidetBarnetrygdSkatt(inneværendeÅr.toString())
        logger.info("Henter perioder med utvidet barnetrygd for å oppdatere cache $inneværendeÅr")
        inneværendeÅrResponse.forEach { barnetrygdService.finnPerioderUtvidetBarnetrygdSkatt(it.ident, inneværendeÅr.value) }

        barnetrygdService.finnPersonerUtvidetBarnetrygdSkatt(inneværendeÅr.minusYears(1).toString())
    }
}