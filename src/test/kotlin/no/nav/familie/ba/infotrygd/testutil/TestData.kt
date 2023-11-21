package no.nav.familie.ba.infotrygd.testutil

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.commons.foedselsnummer.Kjoenn
import no.nav.commons.foedselsnummer.testutils.FoedselsnummerGenerator
import no.nav.familie.ba.infotrygd.model.db2.Delytelse
import no.nav.familie.ba.infotrygd.model.db2.DelytelseId
import no.nav.familie.ba.infotrygd.model.db2.Utbetaling
import no.nav.familie.ba.infotrygd.model.db2.Vedtak
import no.nav.familie.ba.infotrygd.model.dl1.Barn
import no.nav.familie.ba.infotrygd.model.dl1.Hendelse
import no.nav.familie.ba.infotrygd.model.dl1.Person
import no.nav.familie.ba.infotrygd.model.dl1.Sak
import no.nav.familie.ba.infotrygd.model.dl1.SakPerson
import no.nav.familie.ba.infotrygd.model.dl1.Stønad
import no.nav.familie.ba.infotrygd.nextId
import no.nav.familie.ba.infotrygd.utils.DatoUtils
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object TestData {

    fun foedselsNr(
        foedselsdato: LocalDate? = null,
        kjoenn: Kjoenn = Kjoenn.MANN
    ): FoedselsNr {

        return fnrGenerator.foedselsnummer(
            foedselsdato = foedselsdato,
            kjoenn = kjoenn
        )
    }

    fun barn(
        person: Person,
        iverksatt: String = (999999-202005).toString(),
        virkningFom: String = (999999-202005).toString(),
        barnetrygdTom: String = "000000",
        barnFnr: FoedselsNr = foedselsNr(),
        region: String = "X",
        stønadstype: String? = null
    ): Barn {
        return Barn(
            id = nextId(),
            fnr = person.fnr,
            tkNr = person.tkNr,
            personKey = person.personKey,
            barnFnr = barnFnr,
            barnetrygdTom = barnetrygdTom,
            region = region,
            iverksatt = iverksatt,
            virkningFom = virkningFom,
            stønadstype = stønadstype
        )
    }

    fun barn(stønad: Stønad,
             barnFnr: FoedselsNr = foedselsNr(LocalDate.now()),
             barnetrygdTom: String? = null,
             stønadstype: String? = null
    ): Barn {
        return Barn(
            id = nextId(),
            fnr = stønad.fnr!!,
            tkNr = stønad.tkNr,
            personKey = stønad.personKey,
            barnFnr = barnFnr,
            iverksatt = stønad.iverksattFom,
            virkningFom = stønad.virkningFom,
            region = stønad.region,
            barnetrygdTom = barnetrygdTom ?: DatoUtils.stringDatoMMyyyyTilYearMonth(stønad.opphørtFom)
                ?.minusMonths(1)?.format(DateTimeFormatter.ofPattern("MMyyyy")) ?: "000000",
            stønadstype = stønadstype
        )
    }

    fun person(
        fnr: FoedselsNr = foedselsNr(),
        tkNr: String = "1000",
        personKey: Long = tkNr.let { it + fnr.asString }.toLong(),
        region: String = "X"
    ): Person {
        return Person(
            id = nextId(),
            fnr = fnr,
            tkNr = tkNr,
            personKey = personKey,
            region = region,
            mottakerNummer = 80000123123,
            pensjonstrygdet = " "
        )
    }

    fun vedtak(sak: Sak, kodeRutine: String = "BA", kodeResultat: String = "  "): Vedtak {
        return Vedtak(
            vedtakId = 1L,
            stønadId = 1L,
            saksnummer = sak.saksnummer.toLong(),
            saksblokk = sak.saksblokk,
            løpenummer = 1L,
            kodeRutine = kodeRutine,
            kodeResultat = kodeResultat,
            tkNr = sak.tkNr,
            delytelse = listOf(Delytelse(DelytelseId(1, 1), LocalDate.of(2020, 1, 1), LocalDate.now().minusDays(1), 1900.0, "MS", typeUtbetaling = "M"), Delytelse(DelytelseId(1, 2), LocalDate.now(), null, 1940.0, "MS", typeUtbetaling = "M"))
        )
    }

    fun stønad(
        mottaker: Person,
        saksblokk: String = "A",
        saksnummer: String = "01",
        status: String = "01",
        iverksattFom: String = (999999-202005).toString(),
        virkningFom: String = (999999-202005).toString(),
        opphørtIver: String = "000000",
        opphørtFom: String = "000000",
        opphørsgrunn: String? = "M",
        region: String? = null,
        antallBarn:Int = 1
    ): Stønad {
        return Stønad(
            id = nextId(),
            personKey = mottaker.personKey,
            sakNr = saksnummer,
            saksblokk = saksblokk,
            status = status,
            tekstkode = "99",
            iverksattFom = iverksattFom,
            virkningFom = virkningFom,
            fnr = mottaker.fnr,
            tkNr = mottaker.tkNr,
            opphørtIver = opphørtIver,
            opphørtFom = opphørtFom,
            opphørsgrunn = opphørsgrunn,
            region = region ?: mottaker.region,
            antallBarn = antallBarn
        )
    }

    fun sak(person: Person,
            saksblokk: String = "A",
            saksnummer: String = "01",
            valg: String = "OR",
            undervalg: String = "OS"): Sak {
        return Sak(
            id = nextId(),
            personKey = person.personKey,
            saksblokk = saksblokk,
            saksnummer = saksnummer,
            mottattdato = LocalDate.now(),
            regDato = LocalDate.now(),
            region = person.region,
            kapittelNr = "BA",
            valg = valg,
            undervalg = undervalg,
            type = "S",
            resultat = "I",
            vedtaksdato = LocalDate.now(),
            iverksattdato = LocalDate.now(),
            fnr = person.fnr,
        )
    }

    fun utbetaling(
        stønad: Stønad,
        kontonummer: String = "06010000",
        beløp: Double = 1054.00,
        utbetalingTom: String? = null,
    ): Utbetaling {
        return Utbetaling(
            personKey = stønad.personKey,
            startUtbetalingMåned = stønad.iverksattFom,
            virksomFom = stønad.virkningFom,
            kontonummer = kontonummer,
            utbetalingstype = "M",
            beløp = beløp,
            fnr = stønad.fnr!!,
            utbetalingId = nextId(),
            utbetalingTom = utbetalingTom ?: stønad.opphørtFom
        )
    }

    fun sak(stønad: Stønad,
            valg: String = "OR",
            undervalg: String = "OS"): Sak {
        return Sak(
            id = nextId(),
            personKey = stønad.personKey,
            saksblokk = stønad.saksblokk,
            saksnummer = stønad.sakNr,
            mottattdato = LocalDate.now(),
            regDato = LocalDate.now(),
            region = stønad.region,
            kapittelNr = "BA",
            valg = valg,
            undervalg = undervalg,
            type = "S",
            resultat = "I",
            vedtaksdato = LocalDate.now(),
            iverksattdato = LocalDate.now(),
            fnr = stønad.fnr!!,
            tkNr = stønad.tkNr
        )
    }

    fun sakPerson(person: Person): SakPerson {
        return SakPerson(nextId(), person.region, person.personKey, person.fnr)
    }

    fun hendelse(person: Person, aksjondatoSeq: Long, tekstKode: String): Hendelse {
        return Hendelse(
            id = nextId(),
            personKey = person.personKey,
            fnr = person.fnr,
            tkNr = person.tkNr,
            saksnummer = "01",
            saksblokk = "A",
            region = "1",
            aksjonsdatoSeq = aksjondatoSeq,
            tekstKode1 = tekstKode
        )

    }

    private val fnrGenerator = FoedselsnummerGenerator()
}
