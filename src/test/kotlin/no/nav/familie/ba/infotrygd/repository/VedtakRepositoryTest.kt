package no.nav.familie.ba.infotrygd.repository

import no.nav.familie.ba.infotrygd.model.db2.Endring
import no.nav.familie.ba.infotrygd.model.db2.LøpeNrFnr
import no.nav.familie.ba.infotrygd.model.db2.StønadDb2
import no.nav.familie.ba.infotrygd.testutil.TestData
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate

@DataJpaTest
@ActiveProfiles("test")
class VedtakRepositoryTest {

    @Autowired
    lateinit var personRepository: PersonRepository

    @Autowired
    lateinit var sakRepository: SakRepository

    @Autowired
    lateinit var løpeNrFnrRepository: LøpeNrFnrRepository

    @Autowired
    lateinit var stønadDb2Repository: StønadDb2Repository

    @Autowired
    lateinit var endringRepository: EndringRepository


    @Autowired
    lateinit var vedtakRepository: VedtakRepository

    @BeforeEach
    fun setUp() {
        personRepository.deleteAll()
        sakRepository.deleteAll()
        stønadDb2Repository.deleteAll()
        endringRepository.deleteAll()
        vedtakRepository.deleteAll()
    }

    @Test
    fun `hentVedtak - skal returnere vedtak`() {
        val person = personRepository.saveAndFlush(TestData.person()).also {
            løpeNrFnrRepository.saveAndFlush(LøpeNrFnr(1, it.fnr.asString))
        }
        val sak = sakRepository.saveAndFlush(TestData.sak(person))

        val vedtak = vedtakRepository.saveAndFlush(TestData.vedtak(sak)).also {
            stønadDb2Repository.saveAndFlush(StønadDb2(it.stønadId, "BA", 1))
            endringRepository.saveAndFlush(Endring(it.vedtakId, "  "))
        }

        vedtakRepository.hentVedtak(person.fnr.asString, sak.saksnummer.toLong(), sak.saksblokk).also {
            assertThat(it.size).isEqualTo(1)
            assertThat(it.first().vedtakId).isEqualTo(vedtak.vedtakId)
            assertThat(it.first().delytelse.size).isEqualTo(2)
            assertThat(it.first().delytelse.first().id.vedtakId).isEqualTo(1)
            assertThat(it.first().delytelse.first().id.linjeId).isEqualTo(1)
            assertThat(it.first().delytelse.first().fom).isEqualTo(LocalDate.of(2020, 1, 1))
            assertThat(it.first().delytelse.first().tom).isEqualTo(LocalDate.now().minusDays(1))
            assertThat(it.first().delytelse.first().beløp).isEqualTo(1900.0)
            assertThat(it.first().delytelse.last().id.vedtakId).isEqualTo(1)
            assertThat(it.first().delytelse.last().id.linjeId).isEqualTo(2)
            assertThat(it.first().delytelse.last().fom).isEqualTo(LocalDate.now())
            assertThat(it.first().delytelse.last().tom).isNull()
            assertThat(it.first().delytelse.last().beløp).isEqualTo(1940.0)
        }
    }
}
