package no.nav.infotrygd.beregningsgrunnlag.repository

import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.KodeRutine
import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Tema
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
        val fnr = TestData.foedselNr()

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
    }

    @Test
    fun findByFnrAndFom() {
        val fnr = TestData.foedselNr()
        val dato = LocalDate.now()

        val relevant = TestData.vedtak(fnr = fnr, datoStart = dato)
        val forGammel = TestData.vedtak(fnr = fnr, datoStart = dato.minusYears(1))
        val feilKodeRutine = TestData.vedtak(fnr = fnr, datoStart = dato, kodeRutine = KodeRutine.TEST)

        repository.saveAll(listOf(relevant, forGammel, feilKodeRutine))

        val all = repository.findAll()
        val res = repository.findByFnrAndStartDato(fnr, dato.minusDays(1))
        assertThat(res).hasSize(1)

        assertThat(relevant).isEqualTo(res[0])
    }

    @Test
    fun findByFnrAndFomTom() {
        val fnr = TestData.foedselNr()
        val dato = LocalDate.now()

        val relevant = TestData.vedtak(fnr = fnr, datoStart = dato)
        val forGammel = TestData.vedtak(fnr = fnr, datoStart = dato.minusYears(1))
        val forNy = TestData.vedtak(fnr = fnr, datoStart = dato.plusYears(1))
        val feilKodeRutine = TestData.vedtak(fnr = fnr, datoStart = dato, kodeRutine = KodeRutine.TEST)

        repository.saveAll(listOf(relevant, forGammel, forNy, feilKodeRutine))

        val res = repository.findByFnrAndStartDato(fnr, dato.minusDays(1), dato.plusDays(1))
        assertThat(res).hasSize(1)

        assertThat(relevant).isEqualTo(res[0])
    }
}