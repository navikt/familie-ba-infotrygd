package no.nav.familie.ba.infotrygd.service

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class WarmupCacheService(val barnetrygdService: BarnetrygdService) {

    private val logger = LoggerFactory.getLogger(WarmupCacheService::class.java)

    @Scheduled(fixedDelay = 24 * 60 * 60 * 1000, initialDelay = 10 * 60 * 1000)
    fun warmupSkatteetatenPersonerCache() {
        // Varmer opp cache for personer og perioder siste året med særfradrag
        // Varmer også opp cache for personer for 2021 og 2022. Siden skatt også kaller oss for 2021 og 2022
        for (år in 2021..2023) {
            logger.info("Henter personer med utvidet barnetrygd for å oppdatere cache $år")
            barnetrygdService.finnPersonerUtvidetBarnetrygdSkatt(år.toString()).let { resultat ->
                if (år == 2023) {
                    resultat.forEach { barnetrygdService.finnPerioderUtvidetBarnetrygdSkatt(it.ident, 2023) }
                }
            }
        }
    }
}