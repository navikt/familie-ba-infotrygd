package no.nav.familie.ba.infotrygd.service

import io.micrometer.core.instrument.Metrics
import io.micrometer.core.instrument.MultiGauge
import io.micrometer.core.instrument.Tags
import no.nav.familie.ba.infotrygd.repository.StønadRepository
import no.nav.familie.leader.LeaderClient
import org.slf4j.LoggerFactory
import org.springframework.core.env.Environment
import org.springframework.data.domain.Pageable
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class MålerService(private val stønadRepository: StønadRepository, private val environment:Environment) {

    val antallLøpendeSakerGauge = MultiGauge.builder("lopendeSaker").register(Metrics.globalRegistry)
    private val logger = LoggerFactory.getLogger(BarnetrygdService::class.java)


    @Scheduled(fixedDelay = 4 * 60 * 60 * 1000, initialDelay = 3 * 50 * 1000)
    fun antallLøpendeSaker() {
        if (LeaderClient.isLeader() != true) return
        logger.info("Oppdaterer metrikker")
        val rows = mutableListOf<MultiGauge.Row<Number>>()
        if (erPreprod()) {
            val antall = stønadRepository.findKlarForMigreringIPreprod(Pageable.unpaged(),"OR", "OS").size
            rows.add(
                MultiGauge.Row.of(
                    Tags.of("valg", "OR",
                            "undervalg", "OS"),
                    antall))
        } else {
            stønadRepository.countLøpendeStønader().map {
                rows.add(
                    MultiGauge.Row.of(
                        Tags.of("valg", it.valg,
                                "undervalg", it.undervalg),
                        it.antall))
            }
        }
        antallLøpendeSakerGauge.register(rows, true)
    }

    private fun erPreprod(): Boolean {
        return environment.activeProfiles.any {
            it == "preprod"
        }
    }
}