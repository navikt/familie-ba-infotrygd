@file:Suppress("NonAsciiCharacters", "FunctionName", "LocalVariableName")

package no.nav.infotrygd.barnetrygd.service

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.familie.eksterne.kontrakter.skatteetaten.SkatteetatenPeriode
import no.nav.familie.eksterne.kontrakter.skatteetaten.SkatteetatenPerioder
import no.nav.familie.eksterne.kontrakter.skatteetaten.SkatteetatenPerioderResponse
import no.nav.infotrygd.barnetrygd.model.db2.Utbetaling
import no.nav.infotrygd.barnetrygd.model.db2.toDelytelseDto
import no.nav.infotrygd.barnetrygd.model.dl1.*
import no.nav.infotrygd.barnetrygd.model.kodeverk.SakStatus.IKKE_BEHANDLET
import no.nav.infotrygd.barnetrygd.repository.BarnRepository
import no.nav.infotrygd.barnetrygd.repository.PersonRepository
import no.nav.infotrygd.barnetrygd.repository.SakRepository
import no.nav.infotrygd.barnetrygd.repository.StatusRepository
import no.nav.infotrygd.barnetrygd.repository.StønadRepository
import no.nav.infotrygd.barnetrygd.repository.TrunkertStønad
import no.nav.infotrygd.barnetrygd.repository.UtbetalingRepository
import no.nav.infotrygd.barnetrygd.repository.VedtakRepository
import no.nav.infotrygd.barnetrygd.rest.controller.BarnetrygdController.InfotrygdUtvidetBarnetrygdResponse
import no.nav.infotrygd.barnetrygd.rest.controller.BarnetrygdController.Stønadstype.SMÅBARNSTILLEGG
import no.nav.infotrygd.barnetrygd.rest.controller.BarnetrygdController.Stønadstype.UTVIDET
import no.nav.infotrygd.barnetrygd.rest.controller.BarnetrygdController.UtvidetBarnetrygdPeriode
import no.nav.infotrygd.barnetrygd.utils.DatoUtils
import org.slf4j.LoggerFactory
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
            }.distinct()
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

    fun finnPerioderMedUtvidetBarnetrygdForÅr(brukerFnr: FoedselsNr,
                                              år: Int
    ): SkatteetatenPerioderResponse {

        val utvidetBarnetrygdStønader = stonadRepository.findStønadByÅrAndStatusKoderAndFnr(brukerFnr, år, "00", "02", "03").filter { erUtvidetBarnetrygd(it) }

        val perioder = konverterTilDtoUtvidetBarnetrygdForSkatteetaten(brukerFnr, utvidetBarnetrygdStønader)

        return SkatteetatenPerioderResponse(perioder)
    }

    fun finnPersonerMedUtvidetBarnetrygd(år: String): List<TrunkertStønad> {
        return stonadRepository.findStønadByÅrAndStatusKoder(år.toInt(), "00", "02", "03")
            .filter { erUtvidetBarnetrygd(it) }
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
                sakRepository.erUtvidetBarnetrygd(stønad.personKey, stønad.saksblokk, stønad.sakNr, stønad.region)
            }

            2L -> true //Utvidet barnetrygd.
            3L -> true //Sykt barn (Ikke lenger i bruk, kan forekomme i gamle tilfeller),
            else -> false
        }
    }


    private fun konverterTilDtoUtvidetBarnetrygdForSkatteetaten(brukerFnr: FoedselsNr, utvidetBarnetrygdStønader: List<Stønad>
    ): List<SkatteetatenPerioder>
    {
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

        var sisteVedtakPaaIdent:LocalDateTime? = null

        val allePerioder = mutableListOf<SkatteetatenPeriode>()

        utvidetBarnetrygdStønader.forEach {

            if (sisteVedtakPaaIdent == null) {
                sisteVedtakPaaIdent = finnSisteVedtakPåPerson(it.personKey).atDay(1).atStartOfDay()
            }

            allePerioder.add(
                SkatteetatenPeriode(
                    fraMaaned = DatoUtils.seqDatoTilYearMonth(it.virkningFom)!!.toString(),
                    tomMaaned = DatoUtils.stringDatoMMyyyyTilYearMonth(it.opphørtFom)?.toString(), //Leverer siste dato på stønaden eller null hvis løpenden
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
                val (beløp, manueltBeregnet) = kalkulerBeløp(it, utbetaling)

                UtvidetBarnetrygdPeriode(
                    if (utbetaling.erSmåbarnstillegg()) SMÅBARNSTILLEGG else UTVIDET,
                    utbetaling.fom()!!,
                    utbetaling.tom(),
                    beløp,
                    manueltBeregnet
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

    private fun kalkulerBeløp(it: Stønad, utbetaling: Utbetaling): Pair<Double, Boolean> {
        if (utbetaling.erSmåbarnstillegg()) return Pair(utbetaling.beløp, false)

        if (it.status.toInt() != 0) return Pair(finnUtvidetBarnetrygdBeløpNårStønadIkkeHarStatus0(utbetaling), false)

        val erDeltBosted = sakRepository.findBarnetrygdsakerByStønad(it.personKey, "UT", MANUELL_BEREGNING_DELT_BOSTED, it.saksblokk, it.sakNr, it.region).isNotEmpty()


        if (!erDeltBosted && utbetaling.beløp in LIST_MED_GODKJENTE_UTVIDET_BARNETRYGD_BELØP) {
            return Pair(utbetaling.beløp, false)
        } else if( erDeltBosted) {
            if (utbetaling.beløp in listOf((UTVIDET_BARNETRYGD_ELDRE_SATS/2).toDouble(), (UTVIDET_BARNETRYGD_NÅVÆRENDE_SATS/2).toDouble())) return Pair(utbetaling.beløp, false)

            val antallBarnetrygdbarn = barnRepository.findBarnByStønad(it).count()



            // Denne støtter manuell beregning for delt bosted hvor barnetrygd er inkludert i utbetalingsbeløpet med nåværende sats
            if (utbetaling.beløp.toInt() == (antallBarnetrygdbarn * BARNETRYGD_OVER_6ÅR_SATS + UTVIDET_BARNETRYGD_NÅVÆRENDE_SATS) / 2) {
                return Pair(UTVIDET_BARNETRYGD_NÅVÆRENDE_SATS.toDouble()/2, false)
            }

            // Denne støtter manuell beregning for delt bosted hvor barnetrygd er inkludert i utbetalingsbeløpet med eldre sats
            if (utbetaling.beløp.toInt() == (antallBarnetrygdbarn * BARNETRYGD_ELDRE_SATS + UTVIDET_BARNETRYGD_ELDRE_SATS) / 2) {
                return Pair(UTVIDET_BARNETRYGD_ELDRE_SATS.toDouble()/2, false)
            }

            // Denne støtter manuell beregning for delt bosted hvor barnetrygd er inkludert i utbetalingsbeløpet med sats 2021-09
            if (utbetaling.beløp.toInt() == (antallBarnetrygdbarn * BARNETRYGD_UNDER_6ÅR_SATS_FRA_09_2021 + UTVIDET_BARNETRYGD_ELDRE_SATS) / 2) {
                return Pair(UTVIDET_BARNETRYGD_ELDRE_SATS.toDouble()/2, false)
            }

            // Denne støtter manuell beregning for delt bosted hvor barnetrygd er inkludert i utbetalingsbeløpet med 1 barn under 6 år og et  barn over 6 år
            if (utbetaling.beløp.toInt() == (BARNETRYGD_OVER_6ÅR_SATS + BARNETRYGD_UNDER_6ÅR_SATS + UTVIDET_BARNETRYGD_NÅVÆRENDE_SATS) / 2) {
                return Pair(UTVIDET_BARNETRYGD_NÅVÆRENDE_SATS.toDouble()/2, false)
            }

            // Denne støtter manuell beregning for delt bosted hvor barnetrygd er inkludert i utbetalingsbeløpet med 1 barn under 6 år og et  barn over 6 år fra september 2021
            if (utbetaling.beløp.toInt() == (BARNETRYGD_UNDER_6ÅR_SATS_FRA_09_2021 + BARNETRYGD_OVER_6ÅR_SATS + UTVIDET_BARNETRYGD_NÅVÆRENDE_SATS) / 2) {
                return Pair(UTVIDET_BARNETRYGD_NÅVÆRENDE_SATS.toDouble()/2, false)
            }
        }

        logger.info("Klarer ikke beregne utvidet barnetrygdbeløp. Returnerer manueltBeregnet for stønadId=${it.id}")

        return Pair(utbetaling.beløp, true)
    }

    fun finnUtvidetBarnetrygdBeløpNårStønadIkkeHarStatus0(utbetaling: Utbetaling): Double {
        return if (utbetaling.fom()!!.isAfter(YearMonth.of(2019, 2))) UTVIDET_BARNETRYGD_NÅVÆRENDE_SATS.toDouble()
        else UTVIDET_BARNETRYGD_ELDRE_SATS.toDouble()
    }

    private fun slåSammenSammenhengendePerioder(utbetalingerAvEtGittBeløp: List<UtvidetBarnetrygdPeriode>): List<UtvidetBarnetrygdPeriode> {
        return utbetalingerAvEtGittBeløp.sortedBy { it.fomMåned }
            .fold(mutableListOf()) { sammenslåttePerioder, nesteUtbetaling ->
                if (sammenslåttePerioder.lastOrNull()?.tomMåned == nesteUtbetaling.fomMåned.minusMonths(1)) {
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

    fun hentLøpendeStønader(valg: String, undervalg: String, page: Int): Set<String> {
        val løpendeStønaderFnr = stonadRepository.findLøpendeStønader(PageRequest.of(page, 1000))

        return løpendeStønaderFnr.filter {
            sakRepository.findBarnetrygdsakerByStønad(it.personKey, valg, undervalg, it.saksblokk, it.sakNr, it.region)
                .isNotEmpty()
        }.map { it.fnr.asString }.toSet()
    }

    fun finnSisteVedtakPåPerson(personKey: Long): YearMonth {
        return stonadRepository.findSenesteIverksattFomByPersonKey(personKey).let { DatoUtils.seqDatoTilYearMonth(it)!! }
    }

    companion object {

        const val KAPITTEL_BARNETRYGD = "BA"
        const val VALG_UTVIDET_BARNETRYG = "UT"
        const val UTVIDET_BARNETRYGD_ELDRE_SATS = 970
        const val UTVIDET_BARNETRYGD_NÅVÆRENDE_SATS = 1054
        const val BARNETRYGD_OVER_6ÅR_SATS = 1054
        const val BARNETRYGD_UNDER_6ÅR_SATS_FRA_09_2021 = 1654
        const val BARNETRYGD_UNDER_6ÅR_SATS = 1354
        const val BARNETRYGD_ELDRE_SATS = 970
        private val LIST_MED_GODKJENTE_UTVIDET_BARNETRYGD_BELØP = listOf(
            UTVIDET_BARNETRYGD_ELDRE_SATS.toDouble(),
            UTVIDET_BARNETRYGD_NÅVÆRENDE_SATS.toDouble()
        )

        const val MANUELL_BEREGNING_DELT_BOSTED = "MD"
        const val MANUELL_BEREGNING_EØS = "ME"
        const val MANUELL_BEREGNING = "MB"

    }
}
