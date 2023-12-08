@file:Suppress("NonAsciiCharacters", "FunctionName", "LocalVariableName")

package no.nav.familie.ba.infotrygd.service

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.familie.ba.infotrygd.model.converters.ReversedFoedselNrConverter
import no.nav.familie.ba.infotrygd.model.db2.Utbetaling
import no.nav.familie.ba.infotrygd.model.db2.toDelytelseDto
import no.nav.familie.ba.infotrygd.model.dl1.*
import no.nav.familie.ba.infotrygd.model.kodeverk.SakStatus.IKKE_BEHANDLET
import no.nav.familie.ba.infotrygd.repository.BarnRepository
import no.nav.familie.ba.infotrygd.repository.HendelseRepository
import no.nav.familie.ba.infotrygd.repository.PersonRepository
import no.nav.familie.ba.infotrygd.repository.SakRepository
import no.nav.familie.ba.infotrygd.repository.StatusRepository
import no.nav.familie.ba.infotrygd.repository.StønadRepository
import no.nav.familie.ba.infotrygd.repository.Stønadsklasse
import no.nav.familie.ba.infotrygd.repository.UtbetalingRepository
import no.nav.familie.ba.infotrygd.repository.VedtakRepository
import no.nav.familie.ba.infotrygd.rest.controller.BisysController.InfotrygdUtvidetBarnetrygdResponse
import no.nav.familie.ba.infotrygd.rest.controller.BisysController.Stønadstype.SMÅBARNSTILLEGG
import no.nav.familie.ba.infotrygd.rest.controller.BisysController.Stønadstype.UTVIDET
import no.nav.familie.ba.infotrygd.rest.controller.BisysController.UtvidetBarnetrygdPeriode
import no.nav.familie.ba.infotrygd.rest.controller.PensjonController.BarnetrygdPeriode
import no.nav.familie.ba.infotrygd.rest.controller.PensjonController.BarnetrygdTilPensjon
import no.nav.familie.ba.infotrygd.rest.controller.PensjonController.SakstypeEkstern.EØS
import no.nav.familie.ba.infotrygd.rest.controller.PensjonController.SakstypeEkstern.NASJONAL
import no.nav.familie.ba.infotrygd.rest.controller.PensjonController.YtelseProsent
import no.nav.familie.ba.infotrygd.rest.controller.PensjonController.YtelseTypeEkstern
import no.nav.familie.ba.infotrygd.utils.DatoUtils
import no.nav.familie.ba.infotrygd.utils.DatoUtils.isSameOrAfter
import no.nav.familie.eksterne.kontrakter.skatteetaten.SkatteetatenPeriode
import no.nav.familie.eksterne.kontrakter.skatteetaten.SkatteetatenPerioder
import no.nav.familie.eksterne.kontrakter.skatteetaten.SkatteetatenPerioderResponse
import no.nav.familie.eksterne.kontrakter.skatteetaten.SkatteetatenPerson
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.core.env.Environment
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import kotlin.math.roundToInt
import no.nav.familie.kontrakter.ba.infotrygd.Sak as SakDto
import no.nav.familie.kontrakter.ba.infotrygd.Stønad as StønadDto

@Service
class BarnetrygdService(
    private val stonadRepository: StønadRepository,
    private val barnRepository: BarnRepository,
    private val sakRepository: SakRepository,
    private val vedtakRepository: VedtakRepository,
    private val utbetalingRepository: UtbetalingRepository,
    private val statusRepository: StatusRepository,
    private val environment: Environment,
    private val hendelseRepository: HendelseRepository,
    private val personRepository: PersonRepository
) {

    private val logger = LoggerFactory.getLogger(BarnetrygdService::class.java)
    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    fun findStønadByBrukerFnr(brukerFnr: List<FoedselsNr>, historikk: Boolean? = false): List<StønadDto> {
        return if (brukerFnr.isEmpty()) emptyList() else when (historikk) {
            true -> stonadRepository.findStønadByFnr(brukerFnr)
            else -> stonadRepository.findLøpendeStønadByFnr(brukerFnr)
        }.distinct().map { hentDelytelseOgKonverterTilDto(it) }
    }

    fun findStønadByBarnFnr(barnFnr: List<FoedselsNr>, historikk: Boolean? = false): List<StønadDto> {
        return if (barnFnr.isEmpty()) emptyList() else when (historikk) {
            true -> stonadRepository.findStønadByBarnFnr(barnFnr)
            else -> stonadRepository.findLøpendeStønadByBarnFnr(barnFnr)
        }.distinct().map { hentDelytelseOgKonverterTilDto(it) }
    }

    fun findSakerByBrukerFnr(brukerFnr: List<FoedselsNr>): List<SakDto> {
        return brukerFnr.flatMap {
            sakRepository.findBarnetrygdsakerByFnr(it)
        }
            .distinct()
            .map {
                konverterTilDto(it)
            }
    }

    fun findSakerByBarnFnr(barnFnr: List<FoedselsNr>): List<SakDto> {
        return if (barnFnr.isEmpty()) emptyList() else sakRepository.findBarnetrygdsakerByBarnFnr(barnFnr).distinct()
            .map { konverterTilDto(it) }
    }

    fun hentDelytelseOgKonverterTilDto(stønad: Stønad): StønadDto {
        return StønadDto(
            id = stønad.id,
            status = stønad.status,
            tekstkode = stønad.tekstkode,
            iverksattFom = stønad.iverksattFom,
            virkningFom = stønad.virkningFom,
            opphørtIver = stønad.opphørtIver,
            opphørtFom = stønad.opphørtFom,
            opphørsgrunn = stønad.opphørsgrunn,
            barn = hentBarnMedGyldigStønadstypeTilknyttetPerson(stønad.personKey).map { it.toBarnDto() },
            delytelse = stønad.fnr?.let {
                vedtakRepository.hentVedtak(it.asString, stønad.sakNr.trim().toLong(), stønad.saksblokk)
                    .sortedBy { it.vedtakId }
                    .lastOrNull()?.delytelse?.sortedBy { it.id.linjeId }?.map { it.toDelytelseDto() }
            } ?: emptyList(),
            antallBarn = stønad.antallBarn,
            mottakerNummer = hentMottakerNummer(stønad)
        )
    }

    private fun hentBarnMedGyldigStønadstypeTilknyttetPerson(personKey: Long) =
        barnRepository.findBarnByPersonkey(personKey).filter { it.harGyldigStønadstype }

    private val Barn.harGyldigStønadstype
        get() =
            stønadstype.isNullOrBlank() || stønadstype?.trim() !in listOf("N", "FJ", "IN")


    private fun hentMottakerNummer(stønad: Stønad): Long? {
        val mottakerNummer = personRepository.findMottakerNummerByPersonkey(stønad.personKey)
        return if (mottakerNummer == 0L) null else mottakerNummer
    }


    fun konverterTilDto(sak: Sak): SakDto {
        val status = statusRepository.findStatushistorikkForSak(sak).minByOrNull { it.lopeNr }?.status ?: IKKE_BEHANDLET
        return SakDto(
            id = sak.id,
            saksnr = sak.saksnummer,
            saksblokk = sak.saksblokk,
            regDato = sak.regDato,
            mottattdato = sak.mottattdato,
            kapittelnr = sak.kapittelNr,
            valg = sak.valg,
            undervalg = sak.undervalg,
            type = sak.type,
            nivå = sak.nivaa,
            resultat = sak.resultat,
            vedtaksdato = sak.vedtaksdato,
            // minByOrNull er pga at det på en del gamle saker finnes flere stønader på samme sak, noe som egentlig er feil, men må sees på ved migrering.
            stønad = stonadRepository.findStønadBySak(sak).minByOrNull { it.virkningFom.toInt() }
                ?.let { hentDelytelseOgKonverterTilDto(it) },
            iverksattdato = sak.iverksattdato,
            årsakskode = sak.aarsakskode,
            behenEnhet = sak.behenEnhet,
            regAvEnhet = sak.regAvEnhet,
            status = status.kode,
            tkNr = sak.tkNr,
            region = sak.region,
        )
    }

    fun tellAntallÅpneSaker(brukere: List<String>, barn: List<String>?): Long {
        val personer = brukere.toMutableSet()

        if (!barn.isNullOrEmpty()) {
            val personerViaBarn = barnRepository.findBarnByFnrList(barn.map { FoedselsNr(it) })
                .map { it.fnr.asString }
            personer.addAll(personerViaBarn)
        }
        return personer.map { person -> vedtakRepository.tellAntallÅpneSakerPåPerson(person) }.sum()
    }

    fun finnBarnetrygdForPensjon(
        brukerFnr: FoedselsNr,
        fraDato: YearMonth
    ): List<BarnetrygdTilPensjon> {
        val barnetrygdStønader = stonadRepository.findTrunkertStønadMedUtbetalingÅrByFnr(brukerFnr, fraDato.year)
            .filter { erRelevantStønadForPensjon(it) }
            .filter { filtrerStønaderSomErFeilregistrert(it) }

        val perioder = konverterTilDtoForPensjon(barnetrygdStønader, fraDato)

        if (perioder.isEmpty()) {
            return emptyList()
        }

        // Sjekk om det finnes relaterte saker, dvs om barna finnes i andre stønader
        val barnetrygdFraRelaterteSaker = barnRepository.findBarnByFnrList(perioder.map { FoedselsNr(it.personIdent) })
            .filter { it.fnr != brukerFnr && it.harGyldigStønadstype }
            .map { it.fnr }.distinct()
            .mapNotNull { relatertBrukerFnr ->
                BarnetrygdTilPensjon(
                    fnr = relatertBrukerFnr.asString,
                    barnetrygdPerioder = stonadRepository.findTrunkertStønadMedUtbetalingÅrByFnr(relatertBrukerFnr, fraDato.year)
                        .filter { erRelevantStønadForPensjon(it) }
                        .filter { filtrerStønaderSomErFeilregistrert(it) }
                        .let { relaterteBarnetrygdStønader ->
                            konverterTilDtoForPensjon(relaterteBarnetrygdStønader, fraDato)
                        }
                ).takeIf { it.barnetrygdPerioder.isNotEmpty() }
            }

        return barnetrygdFraRelaterteSaker.plus(
            BarnetrygdTilPensjon(
                fnr = brukerFnr.asString,
                barnetrygdPerioder = perioder
            )
        )
    }

    fun finnUtvidetBarnetrygdBisys(
        brukerFnr: FoedselsNr,
        fraDato: YearMonth
    ): InfotrygdUtvidetBarnetrygdResponse {

        val utvidetBarnetrygdStønader = stonadRepository.findStønadByFnr(listOf(brukerFnr)).filter { it.antallBarn > 0 }
            .map { it.tilTrunkertStønad() }
            .filter { erUtvidetBarnetrygd(it) }
            .filter { filtrerStønaderSomErFeilregistrert(it) }
        val perioder = konverterTilDtoUtvidetBarnetrygd(utvidetBarnetrygdStønader)

        return InfotrygdUtvidetBarnetrygdResponse(perioder.filter {
            skalFiltreresPåDato(fraDato, it.fomMåned, it.tomMåned)
        })
    }

    @Cacheable(cacheManager = "perioderCacheManager", value = ["skatt_perioder"], unless = "#result == null")
    fun finnPerioderUtvidetBarnetrygdSkatt(
        brukerFnr: String,
        år: Int
    ): SkatteetatenPerioderResponse {
        val bruker = FoedselsNr(brukerFnr)

        val utvidetBarnetrygdStønader = stonadRepository.findStønadByÅrAndStatusKoderAndFnr(bruker, år, "00", "02", "03")
            .map { it.tilTrunkertStønad() }
            .filter { erUtvidetBarnetrygd(it) }
            .filter { filtrerStønaderSomErFeilregistrert(it) }
            .filter {
                utbetalingRepository.hentUtbetalingerByStønad(it)
                    .any { it.tom() == null || it.tom()!!.year >= år }
            }

        val perioder = konverterTilDtoUtvidetBarnetrygdForSkatteetaten(bruker, utvidetBarnetrygdStønader, år)

        return SkatteetatenPerioderResponse(perioder)
    }


    @Cacheable(cacheManager = "personerCacheManager", value = ["skatt_personer"], unless = "#result == null")
    fun finnPersonerUtvidetBarnetrygdSkatt(år: String): List<SkatteetatenPerson> {
        val stønaderMedAktuelleKoder = stonadRepository.findStønadByÅrAndStatusKoder(år.toInt(), "00", "02", "03")
            .filter { erUtvidetBarnetrygd(it) }
            .filter { filtrerStønaderSomErFeilregistrert(it) }
            .filter {
                val sisteMåned = DatoUtils.stringDatoMMyyyyTilYearMonth(it.opphørtFom)?.minusMonths(1)
                sisteMåned == null || sisteMåned.year >= år.toInt()
            }
            .filter {
                utbetalingRepository.hentUtbetalingerByStønad(it).any { it.tom() == null || it.tom()!!.year >= år.toInt() }
            }

        val personer = mutableMapOf<String, YearMonth>()

        stønaderMedAktuelleKoder.filter { it.fnr != null }
            .forEach {
                if (!personer.containsKey(it.fnr!!.asString)) {
                    personer[it.fnr.asString] = finnSisteVedtakPåPerson(it.personKey)
                }
            }

        return personer.map {
            SkatteetatenPerson(
                ident = it.key,
                sisteVedtakPaaIdent = it.value.atDay(1).atStartOfDay()
            )
        }
    }

    @Cacheable(cacheManager = "personerCacheManager", value = ["pensjon_personer"], unless = "#result == null")
    fun finnPersonerBarnetrygdPensjon(år: String): List<FoedselsNr> {
        logger.info("henter stønader med aktuelle statuskoder år $år")
        val stønaderMedAktuelleKoder = stonadRepository.findStønadMedUtbetalingByÅrAndStatusKoder(år.toInt(), "00", "01", "02")
        logger.info("Fant ${stønaderMedAktuelleKoder.size} stønader med aktuelle statuskoder for år $år")

        return stønaderMedAktuelleKoder.filter {
            filtrerStønaderSomErFeilregistrert(it) && it.erGjeldendeForÅr(år)
        }.mapNotNull {
            it.fnr
        }
    }

    private fun TrunkertStønad.erGjeldendeForÅr(år: String): Boolean {
        val sisteMåned = DatoUtils.stringDatoMMyyyyTilYearMonth(opphørtFom)?.minusMonths(1)
        return sisteMåned == null || sisteMåned.year >= år.toInt()
    }

    fun listUtvidetStønadstyperForPerson(år: Int, fnr:String): List<String> {
        val utvidetBarnetrygdStønader = stonadRepository.findStønadByÅrAndStatusKoderAndFnr(FoedselsNr(fnr), år, "00", "02", "03").map { it.tilTrunkertStønad() }
            .filter { erUtvidetBarnetrygd(it) }
            .filter { filtrerStønaderSomErFeilregistrert(it) }
            .filter {
                val sisteMåned = DatoUtils.stringDatoMMyyyyTilYearMonth(it.opphørtFom)?.minusMonths(1)
                sisteMåned == null || sisteMåned.year >= år.toInt()
            }
            .filter {
                utbetalingRepository.hentUtbetalingerByStønad(it).any { it.tom() == null || it.tom()!!.year >= år }
            }
        return utvidetBarnetrygdStønader.flatMap { hentUndervalg(it) }
    }

    fun finnUtvidetBarnetrygdBeløpNårStønadIkkeHarStatus0(utbetaling: Utbetaling): Double {
        return if (utbetaling.fom()!!.isAfter(YearMonth.of(2019, 2))) UTVIDET_BARNETRYGD_NÅVÆRENDE_SATS.toDouble()
        else UTVIDET_BARNETRYGD_ELDRE_SATS.toDouble()
    }

    fun finnPersonerKlarForMigrering(
        page: Int,
        size: Int,
        valg: String,
        undervalg: String,
    ): Pair<Set<String>, Int> {
        val stønader: Page<Stønad> = if (environment.activeProfiles.contains(PREPROD)) {
            stonadRepository.findKlarForMigreringIPreprod(PageRequest.of(page, size), valg, undervalg)
        } else {
            stonadRepository.findKlarForMigrering(PageRequest.of(page, size), valg, undervalg)
        }

        return Pair(stønader.mapNotNull { it.fnr?.asString }.toSet(), stønader.totalPages)
    }

    fun finnSisteVedtakPåPerson(personKey: Long): YearMonth {
        return stonadRepository.findSenesteIverksattFomByPersonKey(personKey).let { DatoUtils.seqDatoTilYearMonth(it)!! }
    }

    fun findStønadById(id: Long): StønadDto {
        val stønad = stonadRepository.findById(id).orElseThrow { NoSuchElementException("Fant ikke stønad med id $id") }
        return hentDelytelseOgKonverterTilDto(stønad)
    }

    fun findStønad(personIdent: String, tknr: String, iverksattFom: String, virkningFom: String, region: String): StønadDto {
        val personKey = tknr + ReversedFoedselNrConverter().convertToDatabaseColumn(FoedselsNr(personIdent))
        val stønad = stonadRepository.findStønad(personKey.toLong(), iverksattFom, virkningFom, region)
        return hentDelytelseOgKonverterTilDto(stønad)
    }

    fun harSendtBrevForrigeMåned(personidenter: List<FoedselsNr>, brevkoder: List<String>): List<Hendelse> {
        val date = YearMonth.now().minusMonths(1).atDay(1)
        val seqNumber = (99999999 - date.format(DateTimeFormatter.ofPattern("yyyyMMdd")).toLong())

        return hendelseRepository.findHendelseByFnrInAndTekstKoderIn(personidenter, brevkoder, seqNumber)
    }

    private fun filtrerStønaderSomErFeilregistrert(stønad: TrunkertStønad): Boolean {
        return try {
            val opphørtFom = DatoUtils.stringDatoMMyyyyTilYearMonth(stønad.opphørtFom)
            val virkningFom = DatoUtils.seqDatoTilYearMonth(stønad.virkningFom)
            opphørtFom == null || virkningFom!!.isBefore(opphørtFom)
        } catch (e: DateTimeParseException) {
            logger.warn("Kan ikke parse dato på stønad med stønadid: ${stønad.id}")
            false
        }
    }

    private fun skalFiltreresPåDato(fraDato: YearMonth, fom: YearMonth, tom: YearMonth?): Boolean {
        if (fraDato.isBefore(fom)) return true

        return fraDato.isSameOrAfter(fom) && (tom == null || fraDato.isBefore(tom))
    }

    private fun erUtvidetBarnetrygd(
        stønad: TrunkertStønad
    ): Boolean {
        return when (stønad.status.toLong()) {
            0L -> { //Manuell beregning ved Stønadsklasse BA UT MB/MD/ME.

                if (stønad.fnr == null) {
                    logger.info("stønad.fnr var null for stønad med id ${stønad.id}")
                    return false
                }
                hentUndervalg(stønad).any { undervalg ->
                    undervalg in arrayOf(MANUELL_BEREGNING, MANUELL_BEREGNING_DELT_BOSTED, MANUELL_BEREGNING_EØS)
                }
            }

            2L -> true //Utvidet barnetrygd.
            3L -> true //Sykt barn (Ikke lenger i bruk, kan forekomme i gamle tilfeller),
            else -> false
        }
    }

    private fun erRelevantStønadForPensjon(
        stønad: TrunkertStønad
    ): Boolean {
        return when (stønad.status.toLong()) {
            0L -> { // Manuell beregning ved Stønadsklasse BA OR/UT MB/MD/ME.

                if (stønad.fnr == null) {
                    logger.info("stønad.fnr var null for stønad med id ${stønad.id}")
                    return false
                }
                val undervalg = hentValgOgUndervalg(stønad).second
                if (undervalg in arrayOf(MANUELL_BEREGNING, MANUELL_BEREGNING_DELT_BOSTED, MANUELL_BEREGNING_EØS)) {
                    true
                } else {
                    logger.info("Filtrerer vekk stønad(id=${stønad.id}) med undervalg $undervalg")
                    false
                }
            }
            1L -> true  // Ordinær barnetrygd - Maskinell beregning
            2L -> true  // Utvidet barnetrygd - Maskinell beregning.
            3L -> false // Sykt barn (Ikke lenger i bruk, kan forekomme i gamle tilfeller),
            4L -> false // Ordinær barnetrygd - Institusjon
            else -> false
        }
    }


    private fun konverterTilDtoUtvidetBarnetrygdForSkatteetaten(
        brukerFnr: FoedselsNr, utvidetBarnetrygdStønader: List<TrunkertStønad>, år: Int
    ): List<SkatteetatenPerioder> {
        if (utvidetBarnetrygdStønader.isEmpty()) {
            return emptyList()
        }

        var sisteVedtakPaaIdent: LocalDateTime? = null

        val allePerioder = mutableListOf<SkatteetatenPeriode>()

        utvidetBarnetrygdStønader.forEach {
            if (sisteVedtakPaaIdent == null) {
                sisteVedtakPaaIdent = finnSisteVedtakPåPerson(it.personKey).atDay(1)
                    .atStartOfDay() //skatt bruker siste vedtak på en person for å sjekke om de har lest den før. Hvis dato opprettes så leser de den på nytt
            }
            val fraMåned = DatoUtils.seqDatoTilYearMonth(it.virkningFom)!!
            val tomMåned = DatoUtils.stringDatoMMyyyyTilYearMonth(it.opphørtFom)
            allePerioder.add(
                SkatteetatenPeriode(
                    fraMaaned = fraMåned.toString(),
                    tomMaaned = tomMåned?.minusMonths(1)?.toString(), //Leverer siste dato på stønaden eller null hvis løpenden
                    delingsprosent = delingsprosent(it, år)
                )
            )
        }

        //Slå sammen perioder basert på delingsprosent
        val sammenslåttePerioderDelingsprosent =
            allePerioder.groupBy { it.delingsprosent }.values
                .flatMap(::slåSammenSkatteetatenPeriode).toMutableList()

        val sammenslåttePerioderFiltrert =
            sammenslåttePerioderDelingsprosent.filter {// fjerner perioder som ikke er med i årets uttrekk, som kan komme med i sql uttrekket når opphørtFom er første måned i året
                val sisteMåned = it.tomMaaned?.let { tom -> YearMonth.parse(tom) }
                sisteMåned == null || sisteMåned.year >= år
            }
        return if (sammenslåttePerioderFiltrert.isNotEmpty()) {
            listOf(
                SkatteetatenPerioder(
                    ident = brukerFnr.asString,
                    perioder = sammenslåttePerioderFiltrert,
                    sisteVedtakPaaIdent = sisteVedtakPaaIdent!!
                )
            )
        } else {
            emptyList()
        }
    }

    private fun konverterTilDtoForPensjon(
        barnetrygdStønader: List<TrunkertStønad>,
        fraDato: YearMonth
    ): List<BarnetrygdPeriode> {
        if (barnetrygdStønader.isEmpty()) {
            return emptyList()
        }

        val allePerioder = mutableListOf<BarnetrygdPeriode>()

        barnetrygdStønader.forEach { stønad ->
            val utbetalinger = utbetalingRepository.hentUtbetalingerByStønad(stønad).filterNot {
                it.erSmåbarnstillegg()
            }
            val barna = barnRepository.findBarnByPersonkey(stønad.personKey, true).filter {
                it.harDatoSomSamsvarer(stønad) && it.harGyldigStønadstype
            }
            if (stønad.antallBarn != barna.medLøpendeStønadFraDato(stønad.virkningFom).size) {
                secureLogger.warn("Uoverensstemmelse mellom stønad.antallBarn og antallet barn funnet i konverterTilDtoForPensjon:\n" +
                                          "stønad: $stønad \nbarna: $barna")
            }

            allePerioder.addAll(utbetalinger.flatMap { utbetaling ->

                val (valg, undervalg) = hentValgOgUndervalg(stønad)

                barna.filter { it.barnetrygdTom()?.isSameOrAfter(utbetaling.fom()!!) ?: true }.map { barn ->

                    if (barn.barnetrygdTom()?.isBefore(utbetaling.tom() ?: YearMonth.from(LocalDate.MAX)) == true) {
                        secureLogger.warn("barnetrygden for $barn opphørte før $utbetaling og stønadTom burde kanskje vært oppgitt som ${barn.barnetrygdTom()} istedenfor ${utbetaling.tom()}")
                    }

                    BarnetrygdPeriode(
                        ytelseTypeEkstern = when (valg) {
                            "UT" -> YtelseTypeEkstern.UTVIDET_BARNETRYGD
                            else -> YtelseTypeEkstern.ORDINÆR_BARNETRYGD
                        },
                        stønadFom = utbetaling.fom()!!,
                        stønadTom = utbetaling.tom() ?: YearMonth.from(LocalDate.MAX),
                        personIdent = barn.barnFnr.asString,
                        delingsprosentYtelse = ytelseProsent(stønad, undervalg, fraDato.year),
                        sakstypeEkstern = when (undervalg) {
                            "EU", "ME" -> EØS
                            else -> NASJONAL
                        },
                        pensjonstrygdet = when (stønad.pensjonstrygdet) {
                            "J" -> true
                            "N" -> false
                            else -> null
                        },
                        utbetaltPerMnd = utbetaling.beløp.toInt()
                    )
                }.distinct()
            })
        }

        val perioder =
            allePerioder.groupBy { it.personIdent }.values
                .flatMap(::håndterSammenhengendeOgOverlappendePerioder).toMutableList()

        return perioder.filter { it.stønadTom.isSameOrAfter(fraDato) }
    }

    private fun Barn.harDatoSomSamsvarer(stønad: TrunkertStønad): Boolean {
        if (barnetrygdTom()?.isBefore(virkningFom()) == true) // tilhører en feilregistrert stønad
            return false

        return iverksatt == stønad.iverksattFom && virkningFom == stønad.virkningFom ||
                iverksatt().isBefore(stønad.iverksatt())
    }

    private fun TrunkertStønad.iverksatt() = DatoUtils.seqDatoTilYearMonth(iverksattFom)!!

    private fun Barn.iverksatt() = DatoUtils.seqDatoTilYearMonth(iverksatt)!!

    private fun Barn.virkningFom() = DatoUtils.seqDatoTilYearMonth(virkningFom)!!

    private fun List<Barn>.medLøpendeStønadFraDato(seqDato: String) =
        filterNot { it.barnetrygdTom()?.isBefore(DatoUtils.seqDatoTilYearMonth(seqDato)) == true }
            .distinctBy { it.barnFnr }

    private fun delingsprosent(stønad: TrunkertStønad, år: Int): SkatteetatenPeriode.Delingsprosent {
        val undervalg = hentUndervalg(stønad)
        var delingsprosent = SkatteetatenPeriode.Delingsprosent.usikker
        if (undervalg.any { it == "EF" || it == "EU" }) {
            delingsprosent = SkatteetatenPeriode.Delingsprosent._0
        } else if (undervalg.contains("MD")) {
            if (stønad.antallBarn == 1) {
                delingsprosent = SkatteetatenPeriode.Delingsprosent._50
            } else if (stønad.antallBarn < 7) {
                val sumUtbetaltBeløp = utbetalingRepository.hentUtbetalingerByStønad(stønad).sumOf { it.beløp }
                val gyldigeBeløp = utledListeMedGyldigeUtbetalingsbeløp(stønad.antallBarn, år)

                if (gyldigeBeløp.contains(sumUtbetaltBeløp.roundToInt())) {
                    delingsprosent = SkatteetatenPeriode.Delingsprosent._50
                } else {
                    secureLogger.info("Delingsprosent usikker, ident ${stønad.fnr}, sumUtbetaltBeløp: $sumUtbetaltBeløp, gyldigeBeløp: $gyldigeBeløp" +
                                              ", antallBarn: ${stønad.antallBarn}, år: $år")
                }
            }
        }
        return delingsprosent
    }

    private fun ytelseProsent(stønad: TrunkertStønad, undervalg: String?, år: Int): YtelseProsent {
        if (stønad.status.toInt() != 0 ) {
            return YtelseProsent.FULL
        } else if (undervalg == MANUELL_BEREGNING_DELT_BOSTED) {
            if (stønad.antallBarn == 1) {
                return YtelseProsent.DELT
            } else if (stønad.antallBarn < 7) {
                val sumUtbetaltBeløp = utbetalingRepository.hentUtbetalingerByStønad(stønad).sumOf { it.beløp }
                val gyldigeBeløp = utledListeMedGyldigeUtbetalingsbeløp(stønad.antallBarn, år)

                if (gyldigeBeløp.contains(sumUtbetaltBeløp.roundToInt())) {
                    return YtelseProsent.DELT
                } else {
                    secureLogger.info("Ytelseprosent usikker, ident ${stønad.fnr}, sumUtbetaltBeløp: $sumUtbetaltBeløp, gyldigeBeløp: $gyldigeBeløp" +
                                              ", antallBarn: ${stønad.antallBarn}, år: $år")
                }
            }
        }
        return YtelseProsent.USIKKER
    }

    fun utledListeMedGyldigeUtbetalingsbeløp(antallBarn: Int, år: Int): Set<Int> {
        val gyldigeBeløp = mutableSetOf<Int>()
        for (i in 0..antallBarn) {
            val antallBarnUnder6 = i
            val antallBarnOver6 = antallBarn - i

            if (år >= 2022) {
                val utbetalingsbeløpForBarna = antallBarnOver6 * 1054 + antallBarnUnder6 * SATS_BARNETRYGD_UNDER_6_2022
                val beløp = (utbetalingsbeløpForBarna + UTVIDET_BARNETRYGD_NÅVÆRENDE_SATS) / 2
                gyldigeBeløp.add(beløp.roundToInt())
            } else {
                val utbetalingsbeløpForBarna1 = antallBarnOver6 * 1054 + antallBarnUnder6 * SATS_BARNETRYGD_UNDER_6_2020
                val utbetalingsbeløpForBarna2 = antallBarnOver6 * 1054 + antallBarnUnder6 * SATS_BARNETRYGD_UNDER_6_2021
                val beløp1 = (utbetalingsbeløpForBarna1 + UTVIDET_BARNETRYGD_NÅVÆRENDE_SATS) / 2
                val beløp2 = (utbetalingsbeløpForBarna2 + UTVIDET_BARNETRYGD_NÅVÆRENDE_SATS) / 2
                gyldigeBeløp.addAll(setOf(beløp1.roundToInt(), beløp2.roundToInt()))
            }
        }
        return gyldigeBeløp
    }

    private fun konverterTilDtoUtvidetBarnetrygd(
        utvidetBarnetrygdStønader: List<TrunkertStønad>
    ): List<UtvidetBarnetrygdPeriode> {
        if (utvidetBarnetrygdStønader.isEmpty()) {
            return emptyList()
        }


        val allePerioder = mutableListOf<UtvidetBarnetrygdPeriode>()
        utvidetBarnetrygdStønader.forEach {
            val utbetalinger = utbetalingRepository.hentUtbetalingerByStønad(it)
            allePerioder.addAll(utbetalinger.map { utbetaling ->
                val (beløp, manueltBeregnet, deltBosted) = kalkulerBeløp(it, utbetaling)


                UtvidetBarnetrygdPeriode(
                    if (utbetaling.erSmåbarnstillegg()) SMÅBARNSTILLEGG else UTVIDET,
                    utbetaling.fom()!!,
                    utbetaling.tom(),
                    beløp,
                    manueltBeregnet,
                    deltBosted
                )
            })
        }

        val perioder =
            allePerioder.filter { it.stønadstype == UTVIDET }.groupBy { it.beløp }.values
                .flatMap(::slåSammenSammenhengendePerioder).toMutableList()

        perioder.addAll(
            allePerioder.filter { it.stønadstype == SMÅBARNSTILLEGG }.groupBy { it.beløp }.values
                .flatMap(::slåSammenSammenhengendePerioder)
        )

        return perioder
    }

    private fun kalkulerBeløp(stønad: TrunkertStønad, utbetaling: Utbetaling): Triple<Double, Boolean, Boolean> {
        val undervalg = hentUndervalg(stønad)
        val erDeltBosted = undervalg.any { it == MANUELL_BEREGNING_DELT_BOSTED }

        if (utbetaling.erSmåbarnstillegg()) return Triple(utbetaling.beløp, false, erDeltBosted)

        if (stønad.status.toInt() != 0) return Triple(
            finnUtvidetBarnetrygdBeløpNårStønadIkkeHarStatus0(utbetaling),
            false,
            erDeltBosted
        )

        return Triple(utbetaling.beløp, true, erDeltBosted)
    }

    private fun hentUndervalg(stønad: TrunkertStønad) =
        sakRepository.hentUtvidetBarnetrygdsakerForStønad(stønad).map { it.undervalg }.filterNotNull().ifEmpty {
            hentUtvidetBarnetrygdUndervalgFraDb2(stønad).filterNotNull()
        }

    private fun hentValgOgUndervalg(stønad: TrunkertStønad) =
        sakRepository.hentBarnetrygdsakerForStønad(stønad).map {
            it.valg to it.undervalg
        }.filter { it.second != null }.ifEmpty {
            hentBarnetrygdValgOgUndervalgFraDb2(stønad)
        }.distinct().singleOrNull() ?: run {
            secureLogger.info("Manglende/tvetydig stønadsklassifisering for stønad $stønad")
            (null to null)
        }

    private fun håndterSammenhengendeOgOverlappendePerioder(perioder: List<BarnetrygdPeriode>): List<BarnetrygdPeriode> {
        return perioder.sortedBy { it.stønadFom }
            .fold(mutableListOf()) { foregåendePerioder, nestePeriode ->
                val forrigePeriode = foregåendePerioder.lastOrNull()
                val månedenFørNestePeriode = nestePeriode.stønadFom.minusMonths(1)

                if (forrigePeriode?.stønadTom?.isSameOrAfter(månedenFørNestePeriode) == true) {
                    val kanSlåesSammen = forrigePeriode.delingsprosentYtelse == nestePeriode.delingsprosentYtelse
                            && forrigePeriode.pensjonstrygdet == nestePeriode.pensjonstrygdet
                            && forrigePeriode.sakstypeEkstern == nestePeriode.sakstypeEkstern
                            && forrigePeriode.ytelseTypeEkstern == nestePeriode.ytelseTypeEkstern
                            && forrigePeriode.utbetaltPerMnd == nestePeriode.utbetaltPerMnd
                    when {
                        kanSlåesSammen -> foregåendePerioder.apply { add(removeLast().copy(stønadTom = nestePeriode.stønadTom)) }
                        else -> foregåendePerioder.apply { addAll(listOf(removeLast().copy(stønadTom = månedenFørNestePeriode), nestePeriode)) }
                    }
                } else {
                    foregåendePerioder.apply { add(nestePeriode) }
                }
            }
    }

    private fun slåSammenSammenhengendePerioder(utbetalingerAvEtGittBeløp: List<UtvidetBarnetrygdPeriode>): List<UtvidetBarnetrygdPeriode> {
        return utbetalingerAvEtGittBeløp.sortedBy { it.fomMåned }
            .fold(mutableListOf()) { sammenslåttePerioder, nesteUtbetaling ->
                val forrigeUtbetaling = sammenslåttePerioder.lastOrNull()

                if (forrigeUtbetaling?.tomMåned?.isSameOrAfter(nesteUtbetaling.fomMåned.minusMonths(1)) == true
                    && forrigeUtbetaling.manueltBeregnet == nesteUtbetaling.manueltBeregnet
                    && forrigeUtbetaling.deltBosted == nesteUtbetaling.deltBosted
                ) {
                    sammenslåttePerioder.apply { add(removeLast().copy(tomMåned = nesteUtbetaling.tomMåned)) }
                } else sammenslåttePerioder.apply { add(nesteUtbetaling) }
            }
    }

    private fun slåSammenSkatteetatenPeriode(perioderAvEtGittDelingsprosent: List<SkatteetatenPeriode>): List<SkatteetatenPeriode> {
        return perioderAvEtGittDelingsprosent.sortedBy { it.fraMaaned }
            .fold(mutableListOf()) { sammenslåttePerioder, nesteUtbetaling ->
                val nesteUtbetalingFraMåned = YearMonth.parse(nesteUtbetaling.fraMaaned)
                val forrigeUtbetalingTomMåned = sammenslåttePerioder.lastOrNull()?.tomMaaned?.let { YearMonth.parse(it) }

                if (forrigeUtbetalingTomMåned?.isSameOrAfter(nesteUtbetalingFraMåned.minusMonths(1)) == true) {
                    val nySammenslåing = sammenslåttePerioder.removeLast().copy(tomMaaned = nesteUtbetaling.tomMaaned)
                    sammenslåttePerioder.apply { add(nySammenslåing) }
                } else sammenslåttePerioder.apply { add(nesteUtbetaling) }
            }
    }

    private fun hentUtvidetBarnetrygdUndervalgFraDb2(
        stønad: TrunkertStønad
    ) = stønad.fnr?.let {
        vedtakRepository.hentStønadsklassifisering(
            fnr = stønad.fnr.asString,
            tkNr = stønad.personKey.toString().padStart(15, '0').substring(0, 4),
            saksblokk = stønad.saksblokk,
            saksnummer = stønad.sakNr.toLong()
        ).groupBy { stønadsklasse -> stønadsklasse.vedtakId }.values
            .filter { it.kodeNivå2 == "UT" }
            .map { it.kodeNivå3 }
    } ?: emptyList()

    private fun hentBarnetrygdValgOgUndervalgFraDb2(
        stønad: TrunkertStønad
    ) = stønad.fnr?.let {
        vedtakRepository.hentStønadsklassifisering(
            fnr = stønad.fnr.asString,
            tkNr = stønad.personKey.toString().padStart(15, '0').substring(0, 4),
            saksblokk = stønad.saksblokk,
            saksnummer = stønad.sakNr.toLong()
        ).groupBy { stønadsklasse -> stønadsklasse.vedtakId }.values
            .filter { !it.kodeNivå2.isNullOrBlank() }
            .map { it.kodeNivå2!! to it.kodeNivå3 }
    } ?: emptyList()

    private val List<Stønadsklasse>.kodeNivå2: String?
        get() {
            return find { it.kodeNivaa == "02" }?.kodeKlasse  // vil f.eks være "OR" for en sak av type BA OR OS
        }

    private val List<Stønadsklasse>.kodeNivå3: String?
        get() {
            return find { it.kodeNivaa == "03" }?.kodeKlasse  // vil f.eks være "MD" for en sak av type BA OR MD
        }

    companion object {

        const val UTVIDET_BARNETRYGD_ELDRE_SATS = 970
        const val UTVIDET_BARNETRYGD_NÅVÆRENDE_SATS = 1054
        const val SATS_BARNETRYGD_OVER_6 = 1054.0
        const val SATS_BARNETRYGD_UNDER_6_2020 = 1354.0
        const val SATS_BARNETRYGD_UNDER_6_2021 = 1654.0
        const val SATS_BARNETRYGD_UNDER_6_2022 = 1676.0
        const val MANUELL_BEREGNING_DELT_BOSTED = "MD"
        const val MANUELL_BEREGNING_EØS = "ME"
        const val MANUELL_BEREGNING = "MB"
        const val PREPROD = "preprod"
    }
}

private val BarnetrygdPeriode.erOrdinærBarnetrygd: Boolean
    get() = ytelseTypeEkstern == YtelseTypeEkstern.ORDINÆR_BARNETRYGD

private val BarnetrygdPeriode.erUtvidetBarnetrygd: Boolean
    get() = ytelseTypeEkstern == YtelseTypeEkstern.UTVIDET_BARNETRYGD
