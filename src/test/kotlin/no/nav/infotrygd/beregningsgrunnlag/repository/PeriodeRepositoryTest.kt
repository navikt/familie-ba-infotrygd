package no.nav.infotrygd.beregningsgrunnlag.repository

import no.nav.infotrygd.beregningsgrunnlag.model.Periode
import no.nav.infotrygd.beregningsgrunnlag.model.Utbetaling
import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Stoenadstype
import no.nav.infotrygd.beregningsgrunnlag.nextId
import no.nav.infotrygd.beregningsgrunnlag.values.FodselNr
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
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
class PeriodeRepositoryTest {

    @Autowired
    lateinit var repository: PeriodeRepository

    @Before
    fun setUp() {
        repository.deleteAll()
    }

    private val fnr = FodselNr("10000000001")
    private val tema = Stoenadstype.RISIKOFYLT_ARBMILJOE

    @Test
    fun findByFnrAndStoenadstypeAndDates() {
        val dato = LocalDate.now()
        val relevant = periode(tema, arbufoer = dato)
        val feilTema = periode(Stoenadstype.ADOPSJON, arbufoer = dato)
        val forTidlig = periode(tema, arbufoer = dato.minusYears(1))
        val forSen = periode(tema, arbufoer = dato.plusYears(1))

        repository.saveAll(listOf(relevant, feilTema, forTidlig, forSen))

        val result = repository.findByFnrAndStoenadstypeAndDates(fnr, listOf(tema), dato.minusDays(1), dato.plusDays(1))

        assertThat(listOf(relevant)).isEqualTo(result) // relevant hibernate bug: https://hibernate.atlassian.net/browse/HHH-5409
    }

    @Test
    fun findAvsluttedeSakerByFnr() {

        val relevant = periode(tema, "F")

        val utgatt = periode(tema, "F", LocalDate.now().minusYears(2))
        val ikkeFrisk = periode(tema, "X")
        val ikkeSv = periode(Stoenadstype.FOEDSEL, "F")

        repository.saveAll(listOf(relevant, utgatt, ikkeFrisk, ikkeSv))

        val result = repository.findAvsluttedeSakerByFnr(fnr, LocalDate.now().minusYears(1))
        assertThat(listOf(relevant)).isEqualTo(result) // relevant hibernate bug: https://hibernate.atlassian.net/browse/HHH-5409
    }

    @Test
    fun countAvsluttedeSaker() {
        val relevant = periode(tema, "F")
        repository.save(relevant)
        assertThat(repository.countAvsluttedeSaker(LocalDate.now().minusYears(1))).isEqualTo(1)
    }

    @Test
    fun findOpneSakerMedLopendeUtbetalingByFnr() {
        val relevant = periode(tema, " ")

        val frisk = periode(tema, "F")
        val ikkeSv = periode(Stoenadstype.SYKEPENGER, " ")

        repository.saveAll(listOf(relevant, frisk, ikkeSv))

        val result = repository.findOpneSakerMedLopendeUtbetalingByFnr(fnr)
        assertThat(listOf(relevant)).isEqualTo(result) // relevant hibernate bug: https://hibernate.atlassian.net/browse/HHH-5409
    }

    @Test
    fun utbetalinger() {
        var p = periode(tema, "F", LocalDate.now())
        p = p.copy(utbetalinger = listOf(
            Utbetaling(
                id = nextId(),
                personKey = p.personKey,
                arbufoerSeq = p.arbufoerSeq,
                utbetaltFom = LocalDate.now().minusYears(1),
                utbetaltTom = LocalDate.now(),
                grad = null
            )
        ))

        repository.save(p)

        val result = repository.findAvsluttedeSakerByFnr(fnr, LocalDate.now().minusYears(1))
        assertThat(listOf(p)).isEqualTo(result) // relevant hibernate bug: https://hibernate.atlassian.net/browse/HHH-5409
    }

    @Test
    fun findOpneSakerMedLopendeUtbetaling() {
        val relevant = periode(tema, " ")
        repository.save(relevant)

        assertThat(repository.findOpneSakerMedLopendeUtbetaling()).hasSize(1)
    }

    @Test
    fun countOpneSakerMedLopendeUtbetaling() {
        val relevant = periode(tema, " ")
        repository.save(relevant)

        assertThat(repository.countOpneSakerMedLopendeUtbetaling()).isEqualTo(1)
    }

    private fun periode(
        stoenadstype: Stoenadstype,
        frisk: String = " ",
        arbufoer: LocalDate = LocalDate.now()
    ): Periode {
        return Periode(
            id = nextId(),
            personKey = 1,
            arbufoerSeq = 1,
            fnr = fnr,
            stoenadstype = stoenadstype,
            frisk = frisk,
            arbufoer = arbufoer,
            stoppdato = null,
            utbetalinger = listOf(),
            inntekter = listOf(),
            utbetaltFom = null,
            utbetaltTom = null,
            arbufoerOpprinnelig = LocalDate.now(),
            dekningsgrad = null,
            foedselsdatoBarn = null,
            arbeidskategori = null
        )
    }
}