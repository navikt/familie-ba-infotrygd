package no.nav.familie.ba.infotrygd

import no.nav.familie.metrikker.TellAPIEndpunkterIBrukWebConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
@Import(TellAPIEndpunkterIBrukWebConfig::class)
class Main

fun main(args: Array<String>) {
    System.setProperty("oracle.jdbc.fanEnabled", "false")
    runApplication<Main>(*args)
}
