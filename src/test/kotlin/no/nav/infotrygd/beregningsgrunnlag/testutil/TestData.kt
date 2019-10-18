package no.nav.infotrygd.beregningsgrunnlag.testutil

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.commons.foedselsnummer.Kjoenn
import no.nav.commons.foedselsnummer.testutils.FoedselsnummerGenerator
import no.nav.infotrygd.beregningsgrunnlag.model.Inntekt
import no.nav.infotrygd.beregningsgrunnlag.model.Periode
import no.nav.infotrygd.beregningsgrunnlag.model.Utbetaling
import no.nav.infotrygd.beregningsgrunnlag.model.VedtakBarn
import no.nav.infotrygd.beregningsgrunnlag.model.db2.*
import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.*
import no.nav.infotrygd.beregningsgrunnlag.nextId
import java.time.LocalDate
import java.time.LocalDateTime

object TestData {
    fun foedselsNr(
        foedselsdato: LocalDate = LocalDate.now(),
        kjoenn: Kjoenn = Kjoenn.MANN): FoedselsNr {

        return fnrGenerator.foedselsnummer(
            foedselsdato = foedselsdato,
            kjoenn = kjoenn
        )
    }

    private fun individnummer(foedselsdato: LocalDate, kjoenn: Kjoenn): Int {
        for ((individSerie, aarSerie) in FoedselsNr.Companion.tabeller.serier) {
            if (aarSerie.contains(foedselsdato.year)) {
                var res = individSerie.start + nextId().toInt()
                when(kjoenn) {
                    Kjoenn.MANN -> if (res % 2 == 0) res++
                    Kjoenn.KVINNE -> if(res % 2 != 0) res++
                }
                return res
            }
        }
        throw IllegalArgumentException("Fødselsdato må være mellom år 1854 og 2039")
    }

    fun periode(): Periode {
        return Periode(
            id = nextId(),
            personKey = 1,
            arbufoerSeq = 1,
            fnr = foedselsNr(),
            stoenadstype = Stoenadstype.SVANGERSKAP,
            frisk = Frisk.LOPENDE,
            arbufoer = LocalDate.now(),
            stoppdato = null,
            utbetalinger = listOf(),
            inntekter = listOf(),
            utbetaltFom = null,
            utbetaltTom = null,
            arbufoerOpprinnelig = LocalDate.now(),
            dekningsgrad = null,
            foedselsdatoBarn = null,
            arbeidskategori = null,
            tkNr = "1000",
            barnFnr = foedselsNr(),
            stebarnsadopsjon = null,
            regdato = LocalDate.now(),
            brukerId = "br.id",
            inntektsgrunnlagProsent = null,
            morFnr = foedselsNr()
        )
    }

    fun utbetaling(): Utbetaling =
        Utbetaling(
            id = nextId(),
            personKey = 1,
            arbufoerSeq = 1,
            utbetaltTom = LocalDate.now(),
            utbetaltFom = LocalDate.now(),
            grad = null
        )

    fun inntekt(): Inntekt =
        Inntekt(
            id = nextId(),
            personKey = 1,
            arbufoerSeq = 1,
            arbgiverNr = "12345678901",
            loenn = 1.toBigDecimal(),
            periode = Inntektsperiode.MAANEDLIG
        )

    fun inntektStonad(): no.nav.infotrygd.beregningsgrunnlag.model.db2.Inntekt =
        no.nav.infotrygd.beregningsgrunnlag.model.db2.Inntekt(
            stonadId = -1,
            orgNr = 12345678900,
            inntekt = 100.toBigDecimal(),
            inntektFom = LocalDate.now(),
            lopeNr = 1,
            status = "L",
            periode = Inntektsperiode.MAANEDLIG
        )

    data class PeriodeFactory(
        val personKey: Long = nextId(),
        val arbufoerSeq: Long = nextId(),
        val fnr: FoedselsNr = foedselsNr(),
        val barnFnr: FoedselsNr = foedselsNr(),
        val stebarnsadopsjon: String? = null) {

        fun periode(): Periode = TestData.periode().copy(
            personKey = personKey,
            arbufoerSeq = arbufoerSeq,
            stebarnsadopsjon = stebarnsadopsjon,
            fnr = fnr,
            barnFnr = barnFnr
        )

        fun utbetaling(): Utbetaling = TestData.utbetaling().copy(
            personKey = personKey,
            arbufoerSeq = arbufoerSeq
        )

        fun inntekt(): Inntekt = TestData.inntekt().copy(
            personKey = personKey,
            arbufoerSeq = arbufoerSeq
        )

        fun vedtakBarn(): VedtakBarn {
            val periode = periode()
            return VedtakBarn(
                id = nextId(),
                personKey = periode.barnPersonKey!!,
                arbufoerSeq = arbufoerSeq.toString(),
                kode = periode().barnKode,
                dekningsgrad = 100.toBigDecimal()
            )
        }
    }

    fun stonadBs(): StonadBs {
        return StonadBs(
            id = nextId(),
            brukerId = "bruker",
            tidspunktRegistrert = LocalDateTime.now(),
            barn = LopenrFnr(
                id = nextId(),
                fnr = foedselsNr()
            )
        )
    }

    fun stonad(): Stonad {
        return Stonad(
            id = nextId(),
            kodeRutine = "BS",
            datoStart = LocalDate.now(),
            datoOpphoer = LocalDate.now(),
            stonadBs = stonadBs(),
            inntektshistorikk = listOf()
        )
    }

    fun delytelse(): Delytelse {
        return Delytelse(
            vedtakId = -1,
            type = "PN",
            tidspunktRegistrert = LocalDateTime.now(),
            fom = LocalDate.now(),
            tom = LocalDate.now(),
            delytelseSpFaBs = delytelserSpFaBs()
        )
    }

    fun delytelserSpFaBs(): DelytelseSpFaBs {
        return DelytelseSpFaBs(
            vedtakId = -1,
            type = "PN",
            tidspunktRegistrert = LocalDateTime.now(),
            grad = -1
        )
    }

    fun vedtak(
        datoStart: LocalDate = LocalDate.now(),
        fnr: FoedselsNr = foedselsNr(),
        kodeRutine: String = "BS",
        delytelserEksermpler: List<Delytelse> = listOf(),
        arbeidskategori: Arbeidskategori = Arbeidskategori.AMBASSADEPERSONELL
    ): Vedtak {
        val vedtakId = nextId()
        val delytelser = delytelserEksermpler.map { it.copy(
            vedtakId = vedtakId,
            delytelseSpFaBs = it.delytelseSpFaBs?.copy(
                vedtakId = vedtakId,
                type = it.type,
                tidspunktRegistrert = it.tidspunktRegistrert
            )
        ) }

        return Vedtak(
            id = vedtakId,
            stonad = stonad().copy(
                kodeRutine = kodeRutine
            ),
            person = LopenrFnr(id = nextId(), fnr = fnr),
            kodeRutine = kodeRutine,
            datoStart = datoStart,
            vedtakSpFaBs = VedtakSpFaBs(
                vedtakId = vedtakId,
                arbeidskategori = arbeidskategori
            ),
            delytelser = delytelser
        )
    }

    private val fnrGenerator = FoedselsnummerGenerator()
}