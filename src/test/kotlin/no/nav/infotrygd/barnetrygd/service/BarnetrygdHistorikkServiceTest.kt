package no.nav.infotrygd.barnetrygd.service

import no.nav.infotrygd.barnetrygd.repository.BarnRepository
import no.nav.infotrygd.barnetrygd.repository.PersonRepository
import no.nav.infotrygd.barnetrygd.testutil.TestData
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@ContextConfiguration(classes = [BarnetrygdHistorikkService::class])
internal class BarnetrygdHistorikkServiceTest {

    @MockBean
    lateinit var personRepository: PersonRepository

    @MockBean
    lateinit var barnRepository: BarnRepository

    @Autowired
    lateinit var barnetrygdHistorikk: BarnetrygdHistorikkService

    @Test
    fun `finnes skal returnere true`() {
        val soeker = TestData.person()
        val barnFnr = TestData.foedselsNr()

        Mockito.`when`(barnRepository.findByFnrList(listOf(barnFnr))).thenReturn(listOf(TestData.barn(soeker)))
        Mockito.`when`(personRepository.findByFnrList(listOf(soeker.fnr))).thenReturn(listOf())

        val barnResult = barnetrygdHistorikk.finnes(listOf(soeker.fnr), listOf(barnFnr))
        Mockito.`when`(personRepository.findByFnrList(listOf(soeker.fnr))).thenReturn(listOf(soeker))
        val soekerResult =  barnetrygdHistorikk.finnes(listOf(soeker.fnr), null)

        assertThat(barnResult).isTrue()
        assertThat(soekerResult).isTrue()
    }

    @Test
    fun `finnes skal returnere false`() {
        val soekerFnr = TestData.foedselsNr()
        val barnFnr = TestData.foedselsNr()

        Mockito.`when`(barnRepository.findByFnrList(listOf(barnFnr))).thenReturn(listOf())
        Mockito.`when`(personRepository.findByFnrList(listOf(soekerFnr))).thenReturn(listOf())

        val resultEmptyEmpty = barnetrygdHistorikk.finnes(listOf(soekerFnr), listOf(barnFnr))
        val resultEmptyNull = barnetrygdHistorikk.finnes(listOf(soekerFnr), null)

        assertThat(resultEmptyEmpty).isFalse()
        assertThat(resultEmptyNull).isFalse()
    }
}