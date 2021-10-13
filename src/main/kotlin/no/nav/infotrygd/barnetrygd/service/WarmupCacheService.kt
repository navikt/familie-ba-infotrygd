package no.nav.infotrygd.barnetrygd.service

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
        val nextYearResponse = barnetrygdService.finnPersonerMedUtvidetBarnetrygd(inneværendeÅr.plusYears(1).toString())
        logger.info("Henter personer med utvidet barnetrygd for å oppdatere cache $inneværendeÅr")
        val inneværendeÅrResponse = barnetrygdService.finnPersonerMedUtvidetBarnetrygd(inneværendeÅr.toString())
        logger.info("Henter perioder med utvidet barnetrygd for å oppdatere cache $inneværendeÅr")
        inneværendeÅrResponse.forEach { barnetrygdService.finnPerioderMedUtvidetBarnetrygdForÅr(it.ident, inneværendeÅr.value) }
        logger.info("Henter perioder med utvidet barnetrygd for å oppdatere cache ${inneværendeÅr.plusYears(1).value}")
        nextYearResponse.forEach {
            barnetrygdService.finnPerioderMedUtvidetBarnetrygdForÅr(
                it.ident,
                inneværendeÅr.plusYears(1).value
            )
        }
        logger.info("Perioder for neste år lagt i cache")

        barnetrygdService.finnPersonerMedUtvidetBarnetrygd(inneværendeÅr.minusYears(1).toString())
    }
}