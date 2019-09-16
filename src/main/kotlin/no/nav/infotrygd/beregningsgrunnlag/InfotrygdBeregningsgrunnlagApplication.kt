package no.nav.infotrygd.beregningsgrunnlag

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class InfotrygdBeregningsgrunnlagApplication

fun main(args: Array<String>) {
    runApplication<InfotrygdBeregningsgrunnlagApplication>(*args)
}
