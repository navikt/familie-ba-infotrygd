package no.nav.infotrygd.barnetrygd.repository

import io.mockk.mockk
import no.nav.infotrygd.barnetrygd.service.BarnetrygdService
import no.nav.infotrygd.barnetrygd.testutil.TestData
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@DataJpaTest
@ActiveProfiles("test")
class StønadRepositoryTest {

    @Autowired
    lateinit var stønadRepository: StønadRepository

    lateinit var barnetrygdService: BarnetrygdService

    @Before
    fun setUp() {
        stønadRepository.deleteAll()
        barnetrygdService = BarnetrygdService(mockk(), stønadRepository, mockk(), mockk(), mockk(), mockk())
    }

    @Test
    fun `sjekk at antall personer med utvidet barnetrygd er riktig innenfor hvert av årene 2019, 2020 og 2021`() {
        val personFraInneværendeÅr = TestData.person()
        stønadRepository.saveAll(listOf(
            TestData.stønad(TestData.person(), virkningFom = (999999-201901).toString(), status = "01"), // ordinær barnetrygd fra 2019
            TestData.stønad(personFraInneværendeÅr, status = "02"), // utvidet barnetrygd fra 2020
            TestData.stønad(TestData.person(), opphørtFom = "122020", status = "02") // utvidet barnetrygd kun 2020
        ))
        barnetrygdService.finnPersonerMedUtvidetBarnetrygd("2019").also {
            assertThat(it).hasSize(0)
        }
        barnetrygdService.finnPersonerMedUtvidetBarnetrygd("2020").also {
            assertThat(it).hasSize(2)
        }
        barnetrygdService.finnPersonerMedUtvidetBarnetrygd("2021").also {
            assertThat(it).hasSize(1).extracting("fnr").contains(personFraInneværendeÅr.fnr)
        }
    }
}
