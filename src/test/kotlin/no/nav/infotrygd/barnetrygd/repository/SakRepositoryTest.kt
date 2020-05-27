package no.nav.infotrygd.barnetrygd.repository

import no.nav.infotrygd.barnetrygd.model.kodeverk.SakType
import no.nav.infotrygd.barnetrygd.model.kodeverk.SakValg
import no.nav.infotrygd.barnetrygd.testutil.TestData
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@DataJpaTest
@ActiveProfiles("test")
class SakRepositoryTest {
    @Autowired
    private lateinit var repository: SakRepository

    @Test
    fun status() {
        val sak = TestData.sak()
        repository.save(sak)

        val res = repository.findAll()
        assertThat(res).hasSize(1)

        val resSak = res[0]
        assertThat(resSak.statushistorikk).hasSize(1)
    }

    @Test
    fun gyldigValg() {
        val fnr = TestData.foedselsNr()

        for (valg in listOf(SakValg.OP, SakValg.PB, SakValg.OM, SakValg.PN, SakValg.PI, SakValg.PP)) {
            val sak = TestData.sak(fnr = fnr).copy(
                kapittelNr = "BS",
                valg = valg
            )
            repository.save(sak)
            val res = repository.findPaaroerendeSykdomByFnr(fnr)
            assertThat(res).hasSize(1)
            repository.deleteAll()
        }
    }

    @Test
    fun ugyldigKapittelNr() {
        val fnr = TestData.foedselsNr()
        val sak = TestData.sak(fnr = fnr).copy(
            kapittelNr = "XX",
            valg = SakValg.OP
        )

        repository.save(sak)

        assertThat(repository.findPaaroerendeSykdomByFnr(fnr)).isEmpty()
    }

    @Test
    fun ugyldigValg() {
        val fnr = TestData.foedselsNr()
        val sak = TestData.sak(fnr = fnr).copy(
            kapittelNr = "BS",
            valg = SakValg.UGYLDIG
        )

        repository.save(sak)

        assertThat(repository.findPaaroerendeSykdomByFnr(fnr)).isEmpty()
    }

    @Test
    fun relevanteTyper() {
        val relevanteTyper = listOf(SakType.S, SakType.R, SakType.K, SakType.A)

        val fnr = TestData.foedselsNr()

        for(type in SakType.values()) {
            val sak = TestData.sak(fnr = fnr).copy(
                kapittelNr = "BS",
                valg = SakValg.OP,
                type = type
            )
            repository.save(sak)
        }

        val result = repository.findPaaroerendeSykdomByFnr(fnr)
        assertThat(result).hasSameSizeAs(relevanteTyper)
        for(sak in result) {
            assertThat(relevanteTyper).contains(sak.type)
        }
    }
}