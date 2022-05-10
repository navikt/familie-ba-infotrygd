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
    private val hendelseRepository: HendelseRepository
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
            barn = barnRepository.findBarnByPersonkey(stønad.personKey).filter { it.stønadstype.isNullOrBlank() }
                .map { it.toBarnDto() },
            delytelse = vedtakRepository.hentVedtak(stønad.fnr.asString, stønad.sakNr.trim().toLong(), stønad.saksblokk)
                .sortedBy { it.vedtakId }
                .lastOrNull()?.delytelse?.sortedBy { it.id.linjeId }?.map { it.toDelytelseDto() } ?: emptyList()
        )
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
        logger.info("Fant ${stønader.content.size} stønader på side $page")
        var (ikkeFiltrerteStønader, filtrerteStønader) = stønader.content.partition {
            it.antallBarn > 0 && it.antallBarn == barnRepository.findBarnByPersonkey(it.personKey)
                .filter { barn -> barn.stønadstype.isNullOrBlank() }.size
        }
        logger.info("Fant ${ikkeFiltrerteStønader.size} stønader etter filtrering av antall barn i barnRepository ikke er like barn på stønad eller antallBarn = 0")
        filtrerteStønader.forEach {
            secureLogger.info(
                "Filtrerte vekk stønad ${it.id} med ${it.antallBarn} barn: " +
                        "${barnRepository.findBarnByStønad(it).map { it.toString() }}"
            )
        }

        //filterer barn over 18 år
        ikkeFiltrerteStønader = ikkeFiltrerteStønader.filter {
            val barnOver18 = barnRepository.findBarnByStønad(it).filter { barn ->
                barn.barnFnr.foedselsdato.isBefore(LocalDate.now().minusYears(18L))
                        && barn.barnetrygdTom == "000000"
                        && barn.stønadstype.isNullOrBlank()
            }
            if (barnOver18.isEmpty())
                true
            else {
                secureLogger.info(
                    "Filtrerte vekk stønad ${it.id} med ${barnOver18.size} barn over 18: " +
                            "${barnOver18.map { barn -> barn.toString() }}"
                )
                false
            }
        }
        logger.info("Fant ${ikkeFiltrerteStønader.size} etter at filtrering på alder er satt")

        return Pair(ikkeFiltrerteStønader.map { it.fnr.asString }.toSet(), stønader.totalPages)
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
            logger.error("Kan ikke parse dato på stønad med stønadid: ${stønad.id}")
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
                hentUndervalg(stønad).also { stønad.stønadsklasse = it }.any { undervalg ->
                    undervalg in arrayOf(MANUELL_BEREGNING, MANUELL_BEREGNING_DELT_BOSTED, MANUELL_BEREGNING_EØS)
                }
            }

            2L -> true //Utvidet barnetrygd.
            3L -> true //Sykt barn (Ikke lenger i bruk, kan forekomme i gamle tilfeller),
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
                    delingsprosent = delingsprosent(it)
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

    private fun delingsprosent(stønad: TrunkertStønad): SkatteetatenPeriode.Delingsprosent {
        val undervalg = stønad.stønadsklasse.ifEmpty { hentUndervalg(stønad) }
        var delingsprosent = SkatteetatenPeriode.Delingsprosent.usikker
        if (undervalg.any { it == "EF" || it == "EU" }) {
            delingsprosent = SkatteetatenPeriode.Delingsprosent._0
        } else if (undervalg.contains("MD")) {
            delingsprosent = SkatteetatenPeriode.Delingsprosent._50
        }
        return delingsprosent
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
        val undervalg = stønad.stønadsklasse.ifEmpty { hentUndervalg(stønad) }
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
            hentUtvidetBarnetrygdUndervalgFraDb2(stønad).filterNotNull().also {
                if (it.isNotEmpty()) logger.info("Stønad(${stønad.id}) mangler sak i dl1. Hentet undervalg fra db2: $it")
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
        const val MANUELL_BEREGNING_DELT_BOSTED = "MD"
        const val MANUELL_BEREGNING_EØS = "ME"
        const val MANUELL_BEREGNING = "MB"
        const val PREPROD = "preprod"
    }
}
