package no.nav.familie.ba.infotrygd.testutil

import no.nav.familie.ba.infotrygd.model.db2.Beslutning
import no.nav.familie.ba.infotrygd.model.db2.LøpeNrFnr
import no.nav.familie.ba.infotrygd.model.db2.StønadDb2
import no.nav.familie.ba.infotrygd.model.db2.Stønadsklasse
import no.nav.familie.ba.infotrygd.repository.BarnRepository
import no.nav.familie.ba.infotrygd.repository.BeslutningRepository
import no.nav.familie.ba.infotrygd.repository.LøpeNrFnrRepository
import no.nav.familie.ba.infotrygd.repository.PersonRepository
import no.nav.familie.ba.infotrygd.repository.SakRepository
import no.nav.familie.ba.infotrygd.repository.StønadDb2Repository
import no.nav.familie.ba.infotrygd.repository.StønadRepository
import no.nav.familie.ba.infotrygd.repository.StønadsklasseRepository
import no.nav.familie.ba.infotrygd.repository.UtbetalingRepository
import no.nav.familie.ba.infotrygd.repository.VedtakRepository
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.annotation.PostConstruct

@Component
@Profile("demoData")
class DemoData(
    private val personRepository: PersonRepository,
    private val barnRepository: BarnRepository,
    private val stønadRepository: StønadRepository,
    private val sakRepository: SakRepository,
    private val vedtakRepository: VedtakRepository,
    private val løpeNrFnrRepository: LøpeNrFnrRepository,
    private val utbetalingRepository: UtbetalingRepository,
    private val beslutningRepository: BeslutningRepository,
    private val stønadDb2Repository: StønadDb2Repository,
    private val stønadsklasseRepository: StønadsklasseRepository,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @PostConstruct
    fun init() {
        val person = TestData.person()
        val stønad = TestData.stønad(person)
        val sak = TestData.sak(person, stønad.saksblokk, stønad.sakNr)
        val barn = TestData.barn(person, stønad.iverksattFom, stønad.virkningFom)

        personRepository.saveAndFlush(person).also {
            løpeNrFnrRepository.saveAndFlush(LøpeNrFnr(1, it.fnr.asString))
        }
        barnRepository.saveAndFlush(barn)
        sakRepository.saveAndFlush(sak)
        vedtakRepository.saveAndFlush(TestData.vedtak(sak)).also {
            stønadDb2Repository.saveAndFlush(StønadDb2(1, "BA", 1))
            beslutningRepository.saveAndFlush(Beslutning(1, it.vedtakId, "J"))
            stønadsklasseRepository.saveAndFlush(Stønadsklasse(it.vedtakId, "01", sak.kapittelNr))
            stønadsklasseRepository.saveAndFlush(Stønadsklasse(it.vedtakId, "02", sak.valg))
            stønadsklasseRepository.saveAndFlush(Stønadsklasse(it.vedtakId, "03", sak.undervalg!!))
        }
        stønadRepository.saveAndFlush(TestData.stønad(mottaker = person, opphørtFom = LocalDate.now()
            .format(DateTimeFormatter.ofPattern("ddMMyy"))))

        logger.info("Demo fnr.: ${person.fnr.asString}\nDemo barnFnr.: ${barn.barnFnr.asString}")

        lagTestdataForUtvidetBa()
    }

    fun lagTestdataForUtvidetBa() {
        val person = TestData.person()
        val stønadMedFastsattOpphørtFom = TestData.stønad(mottaker = person,
                                                          opphørtFom = YearMonth.now().plusMonths(6)
                                                              .format(DateTimeFormatter.ofPattern("MMyyyy")),
                                                          status = "02")


        val person2 = TestData.person()
        val stønadUtenFastsattOpphørtFom = TestData.stønad(mottaker = person2,
                                                           opphørtFom = "000000",
                                                           status = "00") // må slå opp i SAK-basen for å finne ut om vedtaket er relevant når status er 0:
/*
        << Hvis S10-KAPITTELNR, S10-VALG og S10-UNDERVALG i S10-segmentet = henholdsvis BA, UT og MB/MD/ME, er dette et relevant vedtak.
           Dersom det nå ikke er funnet noe relevant vedtak (B20-segment), returneres svar "Data ikke funnet" >>
*/
        val relevantSak = TestData.sak(person2, stønadUtenFastsattOpphørtFom.saksblokk, stønadUtenFastsattOpphørtFom.sakNr)
                                  .copy(kapittelNr = "BA", valg = "UT", undervalg = "MB")

        personRepository.saveAll(listOf(person, person2))
        sakRepository.saveAndFlush(relevantSak)
        stønadRepository.saveAll(listOf(stønadMedFastsattOpphørtFom, stønadUtenFastsattOpphørtFom))
        utbetalingRepository.saveAll(listOf(TestData.utbetaling(stønadMedFastsattOpphørtFom),
                                            TestData.utbetaling(stønadUtenFastsattOpphørtFom)))


        logger.info("Utvidet barnetrygd demo 1 fnr: ${person.fnr.asString}\n" +
                    "Utvidet barnetrygd demo 2 fnr: ${person2.fnr.asString}")
    }
}