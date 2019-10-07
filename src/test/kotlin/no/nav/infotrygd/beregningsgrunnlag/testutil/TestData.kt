package no.nav.infotrygd.beregningsgrunnlag.testutil

import no.nav.infotrygd.beregningsgrunnlag.model.Inntekt
import no.nav.infotrygd.beregningsgrunnlag.model.Periode
import no.nav.infotrygd.beregningsgrunnlag.model.Utbetaling
import no.nav.infotrygd.beregningsgrunnlag.model.VedtakBarn
import no.nav.infotrygd.beregningsgrunnlag.model.db2.*
import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.*
import no.nav.infotrygd.beregningsgrunnlag.nextId
import no.nav.infotrygd.beregningsgrunnlag.values.FoedselNr
import java.time.LocalDate
import java.time.LocalDateTime

object TestData {
    fun foedselNr(): FoedselNr {
        val fnr: String = (11010100000 + nextId()).toString()
        return FoedselNr(fnr)
    }

    fun periode(): Periode {
        return Periode(
            id = nextId(),
            personKey = 1,
            arbufoerSeq = 1,
            fnr = FoedselNr("01015912345"),
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
            barnFnr = FoedselNr("01019912345"),
            stebarnsadopsjon = null,
            regdato = LocalDate.now(),
            brukerId = "br.id",
            inntektsgrunnlagProsent = null
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

    data class PeriodeFactory(
        val personKey: Long = nextId(),
        val arbufoerSeq: Long = nextId(),
        val fnr: FoedselNr = FoedselNr((10000000000 + nextId()).toString()),
        val stebarnsadopsjon: String? = null) {

        fun periode(): Periode = TestData.periode().copy(
            personKey = personKey,
            arbufoerSeq = arbufoerSeq,
            stebarnsadopsjon = stebarnsadopsjon,
            fnr = fnr
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
                dekningsgrad = 100
            )
        }
    }

    fun stonadBs(): StonadBs {
        return StonadBs(
            id = nextId(),
            brukerId = "bruker"
        )
    }

    fun stonad(): Stonad {
        return Stonad(
            id = nextId(),
            kodeRutine = "BS",
            datoStart = LocalDate.now(),
            datoOpphoer = LocalDate.now(),
            stonadBs = stonadBs()
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
        fnr: FoedselNr = foedselNr(),
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
}