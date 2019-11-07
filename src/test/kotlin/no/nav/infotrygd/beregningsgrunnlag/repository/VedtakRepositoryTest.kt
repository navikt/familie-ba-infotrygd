package no.nav.infotrygd.beregningsgrunnlag.repository

import no.nav.infotrygd.beregningsgrunnlag.testutil.TestData
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import java.time.LocalDate

@RunWith(SpringRunner::class)
@DataJpaTest
@ActiveProfiles("test")
class VedtakRepositoryTest {

    @Autowired
    lateinit var repository: VedtakRepository

    @Test
    fun findByFnr() {
        val fnr = TestData.foedselsNr()

        val vedtak = TestData.vedtak(
            fnr = fnr,
            delytelserEksermpler = listOf(TestData.delytelse())
        )

        repository.save(vedtak)

        val res = repository.findByFnr(fnr)
        assertThat(res).hasSize(1)
        val v = res[0]
        assertThat(v.delytelser).hasSize(1)
        assertThat(v.delytelser[0].delytelseSpFaBs).isNotNull
        assertThat(v.vedtakSpFaBs).isNotNull
    }

    @Test
    fun findByFnrAndFom() {
        val fnr = TestData.foedselsNr()
        val dato = LocalDate.now()

        val relevantBS = TestData.vedtak(
            datoStart = dato,
            fnr = fnr,
            kodeRutine = "BS"
        )
        val relevantBR = TestData.vedtak(
            datoStart = dato,
            fnr = fnr,
            kodeRutine = "BR"
        )
        val forGammel = TestData.vedtak(
            datoStart = dato.minusYears(1),
            fnr = fnr
        )
        val feilKodeRutine = TestData.vedtak(
            datoStart = dato,
            fnr = fnr,
            kodeRutine = "XX"
        )

        repository.saveAll(listOf(relevantBS, relevantBR, forGammel, feilKodeRutine))

        val res = repository.findByFnrAndStartDato(fnr, dato.minusDays(1))
        assertThat(listOf(relevantBR, relevantBS)).containsExactlyInAnyOrderElementsOf(res)
    }

    @Test
    fun findByFnrAndFomTom() {
        val fnr = TestData.foedselsNr()
        val dato = LocalDate.now()

        val relevantBS = TestData.vedtak(
            datoStart = dato,
            fnr = fnr,
            kodeRutine = "BS"
        )
        val relevantBR = TestData.vedtak(
            datoStart = dato,
            fnr = fnr,
            kodeRutine = "BR"
        )
        val forGammel = TestData.vedtak(
            datoStart = dato.minusYears(1),
            fnr = fnr
        )
        val forNy = TestData.vedtak(
            datoStart = dato.plusYears(1),
            fnr = fnr
        )
        val feilKodeRutine = TestData.vedtak(
            datoStart = dato,
            fnr = fnr,
            kodeRutine = "XX"
        )

        repository.saveAll(listOf(relevantBS, relevantBR, forGammel, forNy, feilKodeRutine))

        val res = repository.findByFnrAndStartDato(fnr, dato.minusDays(1), dato.plusDays(1))
        assertThat(listOf(relevantBR, relevantBS)).containsExactlyInAnyOrderElementsOf(res)
    }
}