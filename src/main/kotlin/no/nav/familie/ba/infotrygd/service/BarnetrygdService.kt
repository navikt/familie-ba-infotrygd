@file:Suppress("NonAsciiCharacters", "FunctionName", "LocalVariableName")

package no.nav.familie.ba.infotrygd.service

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.familie.ba.infotrygd.model.db2.Utbetaling
import no.nav.familie.ba.infotrygd.model.db2.toDelytelseDto
import no.nav.familie.ba.infotrygd.model.dl1.*
import no.nav.familie.ba.infotrygd.model.kodeverk.SakStatus.IKKE_BEHANDLET
import no.nav.familie.ba.infotrygd.repository.BarnRepository
import no.nav.familie.ba.infotrygd.repository.PersonRepository
import no.nav.familie.ba.infotrygd.repository.SakRepository
import no.nav.familie.ba.infotrygd.repository.StatusRepository
import no.nav.familie.ba.infotrygd.repository.StønadRepository
import no.nav.familie.ba.infotrygd.repository.TrunkertStønad
import no.nav.familie.ba.infotrygd.repository.UtbetalingRepository
import no.nav.familie.ba.infotrygd.repository.VedtakRepository
import no.nav.familie.ba.infotrygd.rest.controller.BisysController.InfotrygdUtvidetBarnetrygdResponse
import no.nav.familie.ba.infotrygd.rest.controller.BisysController.Stønadstype.SMÅBARNSTILLEGG
import no.nav.familie.ba.infotrygd.rest.controller.BisysController.Stønadstype.UTVIDET
import no.nav.familie.ba.infotrygd.rest.controller.BisysController.UtvidetBarnetrygdPeriode
import no.nav.familie.ba.infotrygd.utils.DatoUtils
import no.nav.familie.eksterne.kontrakter.skatteetaten.SkatteetatenPeriode
import no.nav.familie.eksterne.kontrakter.skatteetaten.SkatteetatenPerioder
import no.nav.familie.eksterne.kontrakter.skatteetaten.SkatteetatenPerioderResponse
import no.nav.familie.eksterne.kontrakter.skatteetaten.SkatteetatenPerson
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.YearMonth
import no.nav.familie.kontrakter.ba.infotrygd.Sak as SakDto
import no.nav.familie.kontrakter.ba.infotrygd.Stønad as StønadDto

@Service
class BarnetrygdService(
    private val personRepository: PersonRepository,
    private val stonadRepository: StønadRepository,
    private val barnRepository: BarnRepository,
    private val sakRepository: SakRepository,
    private val vedtakRepository: VedtakRepository,
    private val utbetalingRepository: UtbetalingRepository,
    private val statusRepository: StatusRepository,
) {

    private val logger = LoggerFactory.getLogger(BarnetrygdService::class.java)

    @Deprecated("Controller-metoden som benytter den er deprecated.")
    fun mottarBarnetrygd(brukerFnr: List<FoedselsNr>, barnFnr: List<FoedselsNr>?): Boolean {
        val personMottarBarnetrygd = brukerFnr.isNotEmpty() && personRepository.findByFnrList(brukerFnr)
            .flatMap { stonadRepository.findByPersonKeyAndRegion(it.personKey, it.region) }
            .isNotEmpty()
        val mottasBarnetrygdForBarn = barnFnr?.let {
            barnRepository.findBarnetrygdBarnInFnrList(it)
                .flatMap { stonadRepository.findByPersonKeyAndRegion(it.personKey, it.region) }
        }?.isNotEmpty() == true

        return personMottarBarnetrygd || mottasBarnetrygdForBarn
    }

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
                logger.info("Konverterer til SakDto for ${it.id} ${it.saksblokk} ${it.saksnummer} ${it.region}")
                konverterTilDto(it)
            }
    }

    fun findSakerByBarnFnr(barnFnr: List<FoedselsNr>): List<SakDto> {
        return if (barnFnr.isEmpty()) emptyList() else sakRepository.findBarnetrygdsakerByBarnFnr(barnFnr).distinct()
            .map { konverterTilDto(it) }
    }

    fun hentDelytelseOgKonverterTilDto(stønad: Stønad): StønadDto {
        return StønadDto(
            status = stønad.status,
            tekstkode = stønad.tekstkode,
            iverksattFom = stønad.iverksattFom,
            virkningFom = stønad.virkningFom,
            opphørtIver = stønad.opphørtIver,
            opphørtFom = stønad.opphørtFom,
            opphørsgrunn = stønad.opphørsgrunn,
            barn = barnRepository.findBarnByStønad(stønad).map { it.toBarnDto() },
            delytelse = vedtakRepository.hentVedtak(stønad.fnr.asString, stønad.sakNr.toLong(), stønad.saksblokk)
                .firstOrNull()?.delytelse?.map { it.toDelytelseDto() } ?: emptyList()
        )
    }

    fun konverterTilDto(sak: Sak): SakDto {
        val status = statusRepository.findStatushistorikkForSak(sak).minByOrNull { it.lopeNr }?.status ?: IKKE_BEHANDLET
        return SakDto(
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
            stønad = stonadRepository.findStønadBySak(sak).minByOrNull { it.virkningFom.toInt() }?.let { hentDelytelseOgKonverterTilDto(it) },
            iverksattdato = sak.iverksattdato,
            årsakskode = sak.aarsakskode,
            behenEnhet = sak.behenEnhet,
            regAvEnhet = sak.regAvEnhet,
            status = status.kode,
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

    fun finnUtvidetBarnetrygd(
        brukerFnr: FoedselsNr,
        fraDato: YearMonth
    ): InfotrygdUtvidetBarnetrygdResponse {

        val utvidetBarnetrygdStønader = stonadRepository.findStønadByFnr(listOf(brukerFnr)).filter { erUtvidetBarnetrygd(it) }

        val perioder = konverterTilDtoUtvidetBarnetrygd(utvidetBarnetrygdStønader)

        return InfotrygdUtvidetBarnetrygdResponse(perioder.filter {
            skalFiltreresPåDato(fraDato, it.fomMåned, it.tomMåned)
        })
    }

    @Cacheable(cacheManager = "perioderCacheManager", value = ["skatt_perioder"], unless = "#result == null")
    fun finnPerioderMedUtvidetBarnetrygdForÅr(brukerFnr: String,
                                              år: Int
    ): SkatteetatenPerioderResponse {
        val bruker = FoedselsNr(brukerFnr)

        val utvidetBarnetrygdStønader = stonadRepository.findStønadByÅrAndStatusKoderAndFnr(bruker, år, "00", "02", "03").filter { erUtvidetBarnetrygd(it) }

        val perioder = konverterTilDtoUtvidetBarnetrygdForSkatteetaten(bruker, utvidetBarnetrygdStønader)

        return SkatteetatenPerioderResponse(perioder)
    }

    @Cacheable(cacheManager = "personerCacheManager", value = ["skatt_personer"], unless = "#result == null")
    fun finnPersonerMedUtvidetBarnetrygd(år: String): List<SkatteetatenPerson> {
        val stønaderMedAktuelleKoder = stonadRepository.findStønadByÅrAndStatusKoder(år.toInt(), "00", "02", "03")
            .filter { erUtvidetBarnetrygd(it) }

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

    private fun skalFiltreresPåDato(fraDato: YearMonth, fom: YearMonth, tom: YearMonth?): Boolean {
        if (fraDato.isBefore(fom)) return true

        return (fraDato.isAfter(fom) || fraDato == fom) && (tom == null || fraDato.isBefore(tom))
    }

    private fun erUtvidetBarnetrygd(
        stønad: Stønad
    ): Boolean {
        return when (stønad.status.toLong()) {
            0L -> { //Manuell beregning ved Stønadsklasse BA UT MB/MD/ME.
                sakRepository.findBarnetrygdsakerByFnr(stønad.fnr)
                    .filter { sak ->
                        sak.saksblokk == stønad.saksblokk &&
                                sak.saksnummer == stønad.sakNr &&
                                sak.kapittelNr == KAPITTEL_BARNETRYGD &&
                                sak.valg == VALG_UTVIDET_BARNETRYG &&
                                sak.undervalg in arrayOf(MANUELL_BEREGNING, MANUELL_BEREGNING_DELT_BOSTED, MANUELL_BEREGNING_EØS)
                    }.isNotEmpty()
            }

            2L -> true //Utvidet barnetrygd.
            3L -> true //Sykt barn (Ikke lenger i bruk, kan forekomme i gamle tilfeller),
            else -> false
        }
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
                sakRepository.erUtvidetBarnetrygd(stønad.fnr, stønad.saksblokk, stønad.sakNr, stønad.region)
            }

            2L -> true //Utvidet barnetrygd.
            3L -> true //Sykt barn (Ikke lenger i bruk, kan forekomme i gamle tilfeller),
            else -> false
        }
    }


    private fun konverterTilDtoUtvidetBarnetrygdForSkatteetaten(brukerFnr: FoedselsNr, utvidetBarnetrygdStønader: List<Stønad>
    ): List<SkatteetatenPerioder>
    {
        if (utvidetBarnetrygdStønader.isEmpty()) {
            return emptyList()
        }

        var sisteVedtakPaaIdent:LocalDateTime? = null

        val allePerioder = mutableListOf<SkatteetatenPeriode>()

        utvidetBarnetrygdStønader.forEach {

            if (sisteVedtakPaaIdent == null) {
                sisteVedtakPaaIdent = finnSisteVedtakPåPerson(it.personKey).atDay(1).atStartOfDay()
            }

            allePerioder.add(
                SkatteetatenPeriode(
                    fraMaaned = DatoUtils.seqDatoTilYearMonth(it.virkningFom)!!.toString(),
                    tomMaaned = DatoUtils.stringDatoMMyyyyTilYearMonth(it.opphørtFom)?.minusMonths(1)?.toString(), //Leverer siste dato på stønaden eller null hvis løpenden
                    delingsprosent = delingsprosent(it)))

        }
        SkatteetatenPerioder(ident = brukerFnr.asString, perioder = allePerioder, sisteVedtakPaaIdent = sisteVedtakPaaIdent!!)

        //Slå sammen perioder basert på delingsprosent
        val sammenslåttePerioderDelingsprosent =
            allePerioder.groupBy { it.delingsprosent }.values
                .flatMap(::slåSammenSkatteetatenPeriode).toMutableList()

        return listOf(SkatteetatenPerioder(ident = brukerFnr.asString, perioder = sammenslåttePerioderDelingsprosent, sisteVedtakPaaIdent = sisteVedtakPaaIdent!!))
    }

    private fun delingsprosent(it: Stønad): SkatteetatenPeriode.Delingsprosent {
        val undervalgSaker = sakRepository.hentUtvidetBarnetrygdsakerForStønad(it).map { it.undervalg }
        var delingsprosent = SkatteetatenPeriode.Delingsprosent.usikker
        if (undervalgSaker.any { it == "EF" || it == "EU" }) {
            delingsprosent = SkatteetatenPeriode.Delingsprosent._0
        } else if (undervalgSaker.contains("MD")) {
            delingsprosent = SkatteetatenPeriode.Delingsprosent._50
        }
        return delingsprosent
    }

    private fun konverterTilDtoUtvidetBarnetrygd(
        utvidetBarnetrygdStønader: List<Stønad>
    ): List<UtvidetBarnetrygdPeriode> {
        logger.info(
            "StønadsID med utvidet barnetrygd = ${
                utvidetBarnetrygdStønader.map {
                    Triple(
                        it.id,
                        it.virkningFom,
                        it.opphørtFom
                    )
                }
            }"
        )
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

    private fun kalkulerBeløp(it: Stønad, utbetaling: Utbetaling): Triple<Double, Boolean, Boolean> {
        val erDeltBosted = sakRepository.findBarnetrygdsakerByStønad(it.fnr, "UT", MANUELL_BEREGNING_DELT_BOSTED, it.saksblokk, it.sakNr, it.region).isNotEmpty()

        if (utbetaling.erSmåbarnstillegg()) return Triple(utbetaling.beløp, false, erDeltBosted)

        if (it.status.toInt() != 0) return Triple(finnUtvidetBarnetrygdBeløpNårStønadIkkeHarStatus0(utbetaling), false, erDeltBosted)

        return Triple(utbetaling.beløp, true, erDeltBosted)
    }

    fun finnUtvidetBarnetrygdBeløpNårStønadIkkeHarStatus0(utbetaling: Utbetaling): Double {
        return if (utbetaling.fom()!!.isAfter(YearMonth.of(2019, 2))) UTVIDET_BARNETRYGD_NÅVÆRENDE_SATS.toDouble()
        else UTVIDET_BARNETRYGD_ELDRE_SATS.toDouble()
    }

    private fun slåSammenSammenhengendePerioder(utbetalingerAvEtGittBeløp: List<UtvidetBarnetrygdPeriode>): List<UtvidetBarnetrygdPeriode> {
        return utbetalingerAvEtGittBeløp.sortedBy { it.fomMåned }
            .fold(mutableListOf()) { sammenslåttePerioder, nesteUtbetaling ->
                if (sammenslåttePerioder.lastOrNull()?.tomMåned == nesteUtbetaling.fomMåned.minusMonths(1)
                    && sammenslåttePerioder.lastOrNull()?.manueltBeregnet == nesteUtbetaling.manueltBeregnet
                    && sammenslåttePerioder.lastOrNull()?.deltBosted == nesteUtbetaling.deltBosted) {
                    sammenslåttePerioder.apply { add(removeLast().copy(tomMåned = nesteUtbetaling.tomMåned)) }
                } else sammenslåttePerioder.apply { add(nesteUtbetaling) }
            }
    }

    private fun slåSammenSkatteetatenPeriode(perioderAvEtGittDelingsprosent: List<SkatteetatenPeriode>): List<SkatteetatenPeriode> {
        return perioderAvEtGittDelingsprosent.sortedBy { it.fraMaaned }
            .fold(mutableListOf()) { sammenslåttePerioder, nesteUtbetaling ->
                val nesteUtbetalingFraaMåned = YearMonth.parse(nesteUtbetaling.fraMaaned)
                if (sammenslåttePerioder.lastOrNull()?.tomMaaned == nesteUtbetalingFraaMåned.minusMonths(1).toString()) {
                    sammenslåttePerioder.apply { add(removeLast().copy(tomMaaned = nesteUtbetaling.tomMaaned)) }
                } else sammenslåttePerioder.apply { add(nesteUtbetaling) }
            }
    }

    fun finnPersonerKlarForMigrering(page: Int, size: Int, valg: String, undervalg: String): Set<String> {
        val stønader = stonadRepository.findKlarForMigrering(PageRequest.of(page, size), valg, undervalg)

        val filtrerteStønader =  stønader.filter{
            barnRepository.findBarnByStønad(it).size == it.antallBarn
        }

        return filtrerteStønader
            .map { it.fnr.asString }.toSet()
    }

    fun finnSisteVedtakPåPerson(personKey: Long): YearMonth {
        return stonadRepository.findSenesteIverksattFomByPersonKey(personKey).let { DatoUtils.seqDatoTilYearMonth(it)!! }
    }

    companion object {

        const val KAPITTEL_BARNETRYGD = "BA"
        const val VALG_UTVIDET_BARNETRYG = "UT"
        const val UTVIDET_BARNETRYGD_ELDRE_SATS = 970
        const val UTVIDET_BARNETRYGD_NÅVÆRENDE_SATS = 1054
        const val MANUELL_BEREGNING_DELT_BOSTED = "MD"
        const val MANUELL_BEREGNING_EØS = "ME"
        const val MANUELL_BEREGNING = "MB"

    }
}
