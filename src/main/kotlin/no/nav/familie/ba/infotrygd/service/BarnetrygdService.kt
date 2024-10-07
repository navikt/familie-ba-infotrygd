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
import no.nav.familie.ba.infotrygd.utils.DatoUtils.isSameOrBefore
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.time.LocalDate
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
                .mapNotNull { it.fnr?.asString }
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
            .mapNotNull { it.fnr }.distinct()
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

    fun finnUtvidetBarnetrygdBeløpNårStønadIkkeHarStatus0(utbetaling: Utbetaling): Double {
        return if (utbetaling.fom()!!.isAfter(YearMonth.of(2019, 2))) UTVIDET_BARNETRYGD_NÅVÆRENDE_SATS.toDouble()
        else UTVIDET_BARNETRYGD_ELDRE_SATS.toDouble()
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

            allePerioder.addAll(utbetalinger.flatMap { utbetaling ->

                val (valg, undervalg) = hentValgOgUndervalg(stønad)

                barna.filter { it.barnetrygdTom()?.isSameOrAfter(utbetaling.fom()!!) ?: true }.map { barn ->
                    val barnetsOpphørsdato =
                        barna.filter { it.barnFnr == barn.barnFnr }.maxOf { it.barnetrygdTom() ?: YearMonth.from(LocalDate.MAX) }
                    val utbetalingTom = utbetaling.tom() ?: YearMonth.from(LocalDate.MAX)

                    BarnetrygdPeriode(
                        ytelseTypeEkstern = when (valg) {
                            "UT" -> YtelseTypeEkstern.UTVIDET_BARNETRYGD
                            else -> YtelseTypeEkstern.ORDINÆR_BARNETRYGD
                        },
                        stønadFom = utbetaling.fom()!!,
                        stønadTom = minOf(utbetalingTom, barnetsOpphørsdato).takeUnless { it.isBefore(utbetaling.fom()) }
                            ?: maxOf(utbetalingTom, barnetsOpphørsdato),
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
                        utbetaltPerMnd = utbetaling.beløp.toInt(),
                        iverksatt = stønad.iverksatt()
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
        try {
            if (barnetrygdTom()?.isBefore(virkningFom()) == true) // tilhører en feilregistrert stønad
                return false

            return iverksatt().isSameOrBefore(stønad.iverksatt()) &&
                    virkningFom().isSameOrBefore(stønad.virkningFom())
        } catch (e: DateTimeParseException) {
            logger.warn("Klarte ikke parse dato på barn(id=$id), stønad(id=${stønad.id})")
            return false
        }
    }

    private fun TrunkertStønad.iverksatt() = DatoUtils.seqDatoTilYearMonth(iverksattFom)!!

    private fun TrunkertStønad.virkningFom() = DatoUtils.seqDatoTilYearMonth(virkningFom)!!

    private fun Barn.iverksatt() = DatoUtils.seqDatoTilYearMonth(iverksatt)!!

    private fun Barn.virkningFom() = DatoUtils.seqDatoTilYearMonth(virkningFom)!!

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
                    val harSammeFomDato = forrigePeriode.stønadFom == nestePeriode.stønadFom
                    val kanSlåesSammen = forrigePeriode.delingsprosentYtelse == nestePeriode.delingsprosentYtelse
                            && forrigePeriode.pensjonstrygdet == nestePeriode.pensjonstrygdet
                            && forrigePeriode.sakstypeEkstern == nestePeriode.sakstypeEkstern
                            && forrigePeriode.ytelseTypeEkstern == nestePeriode.ytelseTypeEkstern
                            && forrigePeriode.utbetaltPerMnd == nestePeriode.utbetaltPerMnd
                    when {
                        harSammeFomDato -> foregåendePerioder.apply { add(maxOf(removeLast(), nestePeriode, compareBy { it.iverksatt })) }
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
    }
}
