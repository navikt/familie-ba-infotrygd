package no.nav.infotrygd.beregningsgrunnlag.service

import no.nav.infotrygd.beregningsgrunnlag.dto.RammevedtakDto
import no.nav.infotrygd.beregningsgrunnlag.repository.PersonkortRepository
import no.nav.infotrygd.beregningsgrunnlag.testutil.TestData
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import java.time.LocalDate

@RunWith(SpringRunner::class)
@SpringBootTest
@ActiveProfiles("test")
class RammevedtakServiceTest {

    @Autowired
    private lateinit var personkortRepository: PersonkortRepository

    @Autowired
    private lateinit var rammevedtakService: RammevedtakService

    @Test
    fun hentRammevedtak() {
        val tekst = "tekst"
        val relevantDato = LocalDate.of(2020, 1, 1)
        val urelevantDato = relevantDato.plusDays(1)
        val fnr = TestData.foedselsNr()

        val kontonummer = RammevedtakService.KONTONUMMER_OM
        val relevant = TestData.personkort(
            tekst = tekst,
            fnr = fnr,
            dato = relevantDato,
            fom = relevantDato,
            tom = relevantDato,
            kontonummer = kontonummer
        )

        val urelevant = TestData.personkort(
            fnr = fnr,
            dato = urelevantDato,
            fom = urelevantDato,
            tom = urelevantDato,
            kontonummer = kontonummer
        )

        personkortRepository.saveAll(listOf(relevant, urelevant))

        val resultat = rammevedtakService.hentRammevedtak(kontonummer, fnr, relevantDato, relevantDato)
        val forventet = listOf(RammevedtakDto(
            tekst = tekst,
            fom = relevantDato,
            tom = relevantDato,
            date = relevantDato
        ))
        assertThat(resultat).isEqualTo(forventet)
    }

    @Test
    fun hentRammevedtakMedUbegrensetTom() {
        val tekst = "tekst"
        val relevantDato = LocalDate.of(2020, 1, 1)
        val fnr = TestData.foedselsNr()

        val kontonummer = RammevedtakService.KONTONUMMER_OM
        val relevant = TestData.personkort(
            tekst = tekst,
            fnr = fnr,
            dato = relevantDato,
            fom = relevantDato,
            tom = relevantDato,
            kontonummer = kontonummer
        )

        personkortRepository.save(relevant)

        val resultat = rammevedtakService.hentRammevedtak(kontonummer, fnr, LocalDate.MIN, null)
        val forventet = listOf(RammevedtakDto(
            tekst = tekst,
            fom = relevantDato,
            tom = relevantDato,
            date = relevantDato
        ))
        assertThat(resultat).isEqualTo(forventet)
    }
}