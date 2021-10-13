package no.nav.familie.ba.infotrygd.rest.controller

import no.nav.familie.ba.infotrygd.integration.TableIntegrator
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import javax.transaction.Transactional

@RestController
@Unprotected
@Transactional
class TableController(private val tableIntegrator: TableIntegrator) {
    @GetMapping(path = ["/tables"])
    fun get(): Map<String, List<String>> {
        return tableIntegrator.tables
    }
}