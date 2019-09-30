package no.nav.infotrygd.beregningsgrunnlag.repository

import no.nav.infotrygd.beregningsgrunnlag.model.Periode
import no.nav.infotrygd.beregningsgrunnlag.model.Utbetaling
import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Frisk
import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Stoenadstype
import no.nav.infotrygd.beregningsgrunnlag.nextId
import no.nav.infotrygd.beregningsgrunnlag.testutil.TestData
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

    private fun periode(
        stoenadstype: Stoenadstype,
        frisk: Frisk = Frisk.LOPENDE,
        arbufoer: LocalDate = LocalDate.now()
    ): Periode {
        return TestData.periode().copy(
            fnr = fnr,
            stoenadstype = stoenadstype,
            frisk = frisk,
            arbufoer = arbufoer
        )
    }
}