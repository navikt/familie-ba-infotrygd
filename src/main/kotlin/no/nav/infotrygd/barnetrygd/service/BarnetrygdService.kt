@file:Suppress("NonAsciiCharacters", "FunctionName", "LocalVariableName")

package no.nav.infotrygd.barnetrygd.service

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.infotrygd.barnetrygd.model.db2.Utbetaling
import no.nav.infotrygd.barnetrygd.model.db2.toDelytelseDto
import no.nav.infotrygd.barnetrygd.model.dl1.*
import no.nav.infotrygd.barnetrygd.repository.BarnRepository
import no.nav.infotrygd.barnetrygd.repository.PersonRepository
import no.nav.infotrygd.barnetrygd.repository.SakRepository
import no.nav.infotrygd.barnetrygd.repository.StønadRepository
import no.nav.infotrygd.barnetrygd.repository.UtbetalingRepository
import no.nav.infotrygd.barnetrygd.repository.VedtakRepository
import no.nav.infotrygd.barnetrygd.rest.controller.BarnetrygdController
import no.nav.infotrygd.barnetrygd.rest.controller.BarnetrygdController.Stønadstype.SMÅBARNSTILLEGG
import no.nav.infotrygd.barnetrygd.rest.controller.BarnetrygdController.Stønadstype.UTVIDET
import no.nav.infotrygd.barnetrygd.rest.controller.BarnetrygdController.UtvidetBarnetrygdPeriode
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.time.YearMonth
import java.time.format.DateTimeFormatter
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
        return brukerFnr.flatMap { sakRepository.findBarnetrygdsakerByFnr(it) }.distinct()
            .map { konverterTilDto(it) }
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
            barn = stønad.barn.map { it.toBarnDto() },
            delytelse = vedtakRepository.hentVedtak(stønad.fnr.asString, stønad.sakNr.toLong(), stønad.saksblokk)
                .firstOrNull()?.delytelse?.map { it.toDelytelseDto() } ?: emptyList()
        )
    }

    fun konverterTilDto(sak: Sak): SakDto {
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
            stønad = sak.stønadList.distinct().map { hentDelytelseOgKonverterTilDto(it) }.firstOrNull(),
            iverksattdato = sak.iverksattdato,
            årsakskode = sak.aarsakskode,
            behenEnhet = sak.behenEnhet,
            regAvEnhet = sak.regAvEnhet,
            status = sak.status.kode,
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

    fun finnUtvidetBarnetrygd(brukerFnr: FoedselsNr, fraDato: YearMonth): BarnetrygdController.InfotrygdUtvidetBarnetrygdResponse {
        val utvidetBarnetrygdStønader = mutableListOf<Stønad>()

        stonadRepository.findStønadByFnr(listOf(brukerFnr)).forEach { stønad ->
            if (stønad.opphørtFom == NULL_DATO) {
                if (erUtvidetBarnetrygd(stønad)) {
                    utvidetBarnetrygdStønader.add(stønad)
                }
            } else {
                val opphørtFomYearMonth = YearMonth.parse(stønad.opphørtFom, DateTimeFormatter.ofPattern("MMyyyy"))
                if (opphørtFomYearMonth.isAfter(fraDato) && erUtvidetBarnetrygd(stønad)) {
                    utvidetBarnetrygdStønader.add(stønad)
                }
            }
        }

        return konverterTilDtoUtvidetBarnetrygd(utvidetBarnetrygdStønader, brukerFnr)
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
                                sak.undervalg in arrayOf("MB", "MD", "ME")
                    }.isNotEmpty()
            }

            2L -> true //Utvidet barnetrygd.
            3L -> true //Sykt barn (Ikke lenger i bruk, kan forekomme i gamle tilfeller),
            else -> false
        }
    }


    private fun konverterTilDtoUtvidetBarnetrygd(utvidetBarnetrygdStønader: List<Stønad>, brukerFnr: FoedselsNr): BarnetrygdController.InfotrygdUtvidetBarnetrygdResponse  {
        logger.info("StønadsID med utvidet barnetrygd = ${utvidetBarnetrygdStønader.map { Triple(it.id, it.virkningFom, it.opphørtFom) }}")
        if (utvidetBarnetrygdStønader.isEmpty()) {
            return BarnetrygdController.InfotrygdUtvidetBarnetrygdResponse(emptyList())
        }


        val allePerioder = mutableListOf<UtvidetBarnetrygdPeriode>()
        utvidetBarnetrygdStønader.forEach {
            val utbetalinger = utbetalingRepository.hentUtbetalingerByStønad(it)
            allePerioder.addAll(utbetalinger.map { utbetaling ->
                val erSmåbarnstillegg = utbetaling.kontonummer == "06040000"
                UtvidetBarnetrygdPeriode(
                    if (erSmåbarnstillegg) SMÅBARNSTILLEGG else UTVIDET,
                    utbetaling.fom()!!,
                    utbetaling.tom(),
                    if (it.status.toInt() == 0 || erSmåbarnstillegg) utbetaling.beløp else finnUtvidetBarnetrygdBeløpNårStønadIkkeHarStatus0(
                        utbetaling
                    )
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

        return BarnetrygdController.InfotrygdUtvidetBarnetrygdResponse(perioder)
    }

    fun finnUtvidetBarnetrygdBeløpNårStønadIkkeHarStatus0(utbetaling: Utbetaling): Double {
        return if (utbetaling.fom()!!.isAfter(YearMonth.of(2019, 2))) 1054.0
               else 970.00
    }

    private fun slåSammenSammenhengendePerioder(utbetalingerAvEtGittBeløp: List<UtvidetBarnetrygdPeriode>): List<UtvidetBarnetrygdPeriode> {
        return utbetalingerAvEtGittBeløp.sortedBy { it.fomMåned }
            .fold(mutableListOf()) { sammenslåttePerioder, nesteUtbetaling ->
                if (sammenslåttePerioder.lastOrNull()?.tomMåned == nesteUtbetaling.fomMåned.minusMonths(1)) {
                    sammenslåttePerioder.apply { add(removeLast().copy(tomMåned = nesteUtbetaling.tomMåned)) }
                }
                else sammenslåttePerioder.apply { add(nesteUtbetaling) }
            }
    }

    fun hentLøpendeStønader(valg: String, undervalg: String, page: Int): Set<String> {
        val løpendeStønaderFnr = stonadRepository.findLøpendeStønader(PageRequest.of(page, 1000))

        return løpendeStønaderFnr.filter {
            sakRepository.findBarnetrygdsakerByStønad(it.personKey, valg, undervalg, it.saksblokk, it.sakNr, it.region).isNotEmpty()
        }.map { it.fnr.asString }.toSet()
    }

    companion object {

        const val NULL_DATO = "000000"
        const val KAPITTEL_BARNETRYGD = "BA"
        const val VALG_UTVIDET_BARNETRYG = "UT"
    }
}
