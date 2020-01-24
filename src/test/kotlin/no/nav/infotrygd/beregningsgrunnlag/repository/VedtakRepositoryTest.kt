package no.nav.infotrygd.beregningsgrunnlag.repository

import no.nav.commons.foedselsnummer.FoedselsNr
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

        val relevantBS = TestData.vedtak(
            fnr = fnr,
            kodeRutine = "BS"
        )
        val relevantBR = TestData.vedtak(
            fnr = fnr,
            kodeRutine = "BR"
        )

        val feilKodeRutine = TestData.vedtak(
            fnr = fnr,
            kodeRutine = "XX"
        )

        repository.saveAll(listOf(relevantBS, relevantBR, feilKodeRutine))

        val res = repository.findByFnrAndStartDato(fnr)
        assertThat(listOf(relevantBR, relevantBS)).containsExactlyInAnyOrderElementsOf(res)
    }

    @Test
    fun findByFnrAndFomUtenPN() {
        val fnr = TestData.foedselsNr()
        val dato = LocalDate.now()

        val relevant = TestData.vedtak(
            datoStart = dato,
            fnr = fnr,
            kodeRutine = "BS",
            delytelserEksermpler = listOf(
                TestData.delytelse().copy(
                    type = "PN"
                )
            )
        )

        val urelevant = TestData.vedtak(
            datoStart = dato,
            fnr = fnr,
            kodeRutine = "BS",
            delytelserEksermpler = listOf(
                TestData.delytelse().copy(
                    type = "OM"
                )
            )
        )

        repository.saveAll(listOf(relevant, urelevant))
        val res = repository.findByFnrAndStartDato(fnr)
        assertThat(listOf(relevant)).isEqualTo(res)
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
        val feilKodeRutine = TestData.vedtak(
            datoStart = dato,
            fnr = fnr,
            kodeRutine = "XX"
        )

        repository.saveAll(listOf(relevantBS, relevantBR, feilKodeRutine))

        val res = repository.findByFnrAndStartDato(fnr)
        assertThat(listOf(relevantBR, relevantBS)).containsExactlyInAnyOrderElementsOf(res)
    }

    @Test
    fun findByFnrAndFomTomUtenPN() {
        val fnr = TestData.foedselsNr()
        val dato = LocalDate.now()

        val relevant = TestData.vedtak(
            datoStart = dato,
            fnr = fnr,
            kodeRutine = "BS",
            delytelserEksermpler = listOf(
                TestData.delytelse().copy(
                    type = "PN"
                )
            )
        )

        val urelevant = TestData.vedtak(
            datoStart = dato,
            fnr = fnr,
            kodeRutine = "BS",
            delytelserEksermpler = listOf(
                TestData.delytelse().copy(
                    type = "OM"
                )
            )
        )

        repository.saveAll(listOf(relevant, urelevant))
        val res = repository.findByFnrAndStartDato(fnr)
        assertThat(listOf(relevant)).isEqualTo(res)
    }

    @Test
    fun findByBarnFnr() {
        val fnrBarn = TestData.foedselsNr()
        val fnrMor = TestData.foedselsNr()
        val fnrFar = TestData.foedselsNr()

        val fnrUrelevantBarn = TestData.foedselsNr()

        val vedtakMor = vedtak(fnrMor, fnrBarn)
        val vedtakFar = vedtak(fnrFar, fnrBarn)
        val vedtakUrelevant = vedtak(fnrFar, fnrUrelevantBarn)

        repository.saveAll(listOf(vedtakMor, vedtakFar, vedtakUrelevant))

        val result = repository.findBSByFnrBarn(fnrBarn)
        assertThat(result.map { it.id }).containsExactlyInAnyOrder(vedtakMor.id, vedtakFar.id)
    }

    private fun vedtak(
        fnrForelder: FoedselsNr,
        fnrBarn: FoedselsNr
    ) = TestData.vedtak(
        fnr = fnrForelder,
        stonad = TestData.stonad().copy(stonadBs = TestData.stonadBs(fnrBarn = fnrBarn))
    )
}