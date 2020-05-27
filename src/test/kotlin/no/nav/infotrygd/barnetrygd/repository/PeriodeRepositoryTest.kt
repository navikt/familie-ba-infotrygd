package no.nav.infotrygd.barnetrygd.repository

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.infotrygd.barnetrygd.model.Periode
import no.nav.infotrygd.barnetrygd.model.kodeverk.Frisk
import no.nav.infotrygd.barnetrygd.model.kodeverk.Stoenadstype
import no.nav.infotrygd.barnetrygd.testutil.TestData
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

    private val fnr = TestData.foedselsNr()
    private val tema = Stoenadstype.RISIKOFYLT_ARBMILJOE

    @Test
    fun findByFnrAndStoenadstype() {
        val dato = LocalDate.now()
        val relevant = periode(tema, arbufoer = dato)
        val historikk = periode(tema, frisk = Frisk.HISTORIKK)
        val feilTema = periode(Stoenadstype.ADOPSJON, arbufoer = dato)

        repository.saveAll(listOf(relevant, historikk, feilTema))

        val result = repository.findByFnrAndStoenadstype(fnr, listOf(tema))

        assertThat(listOf(relevant)).isEqualTo(result) // relevant hibernate bug: https://hibernate.atlassian.net/browse/HHH-5409
    }

    @Test
    fun findByBarnFnr() {
        val gyldigeStoenadstyper = listOf(
            Stoenadstype.BARNS_SYKDOM,
            Stoenadstype.ALV_SYKT_BARN,
            Stoenadstype.KURS_KAP_3_23,
            Stoenadstype.PAS_DOEDSSYK,
            Stoenadstype.PLEIEPENGER_INSTOPPH
        )
        val ugyldigeStoenadstyper = listOf(Stoenadstype.PLEIEPENGER_NY_ORDNING)

        val gyldigeFrisk = listOf(
            Frisk.LOPENDE,
            Frisk.DOEDSSYK,
            Frisk.EGENMELDING
        )
        val ugyldigeFrisk = listOf(
            Frisk.AVVIST,
            Frisk.BARN,
            Frisk.FRISKMELDT,
            Frisk.HISTORIKK,
            Frisk.PASSIV,
            Frisk.TILBAKEKJOERT
        )

        val barnFnr = TestData.foedselsNr()
        val urelevantFnr = TestData.foedselsNr()

        for(stoenadstype in gyldigeStoenadstyper + ugyldigeStoenadstyper) {
            for(frisk in gyldigeFrisk + ugyldigeFrisk) {
                val periodeBarn = periode(
                    stoenadstype = stoenadstype,
                    frisk = frisk,
                    barnFnr = barnFnr
                )
                val periodeUrelevant = periode(
                    stoenadstype = stoenadstype,
                    frisk = frisk,
                    barnFnr = urelevantFnr
                )

                repository.saveAll(listOf(periodeBarn, periodeUrelevant))
            }
        }

        val res = repository.findByBarnFnr(barnFnr)

        val fnrRes = res.map { it.morFnr }.toSet()
        val stoenadstypeRes = res.map { it.stoenadstype }.toSet()
        val friskRes = res.map { it.frisk }.toSet()

        assertThat(fnrRes).containsOnly(barnFnr)
        assertThat(stoenadstypeRes).containsExactlyInAnyOrderElementsOf(gyldigeStoenadstyper)
        assertThat(friskRes).containsExactlyInAnyOrderElementsOf(gyldigeFrisk)
    }

    private fun periode(
        stoenadstype: Stoenadstype,
        frisk: Frisk = Frisk.LOPENDE,
        arbufoer: LocalDate = LocalDate.now(),
        barnFnr: FoedselsNr? = null
    ): Periode {
        return TestData.periode().copy(
            fnr = fnr,
            stoenadstype = stoenadstype,
            frisk = frisk,
            arbufoer = arbufoer,
            morFnr = barnFnr
        )
    }
}