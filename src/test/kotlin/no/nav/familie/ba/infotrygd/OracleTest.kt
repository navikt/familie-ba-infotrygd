package no.nav.familie.ba.infotrygd

import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.test.context.junit4.SpringRunner
import org.testcontainers.containers.OracleContainer


@RunWith(SpringRunner::class)
@Ignore
class OracleTest {
    @get:Rule
    val oracle = OracleContainer("navoracle:12")
        .withStartupTimeoutSeconds(15)
        .withConnectTimeoutSeconds(1)

    @Test
    fun test() {

    }
}