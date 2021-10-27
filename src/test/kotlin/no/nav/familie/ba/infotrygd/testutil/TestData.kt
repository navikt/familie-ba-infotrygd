package no.nav.familie.ba.infotrygd.testutil

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.commons.foedselsnummer.Kjoenn
import no.nav.commons.foedselsnummer.testutils.FoedselsnummerGenerator
import no.nav.familie.ba.infotrygd.model.db2.Delytelse
import no.nav.familie.ba.infotrygd.model.db2.Utbetaling
import no.nav.familie.ba.infotrygd.model.db2.Vedtak
import no.nav.familie.ba.infotrygd.model.dl1.Barn
import no.nav.familie.ba.infotrygd.model.dl1.Person
import no.nav.familie.ba.infotrygd.model.dl1.Sak
import no.nav.familie.ba.infotrygd.model.dl1.SakPerson
import no.nav.familie.ba.infotrygd.model.dl1.Stønad
import no.nav.familie.ba.infotrygd.nextId
import java.time.LocalDate

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
        region: String = "X"
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
            region = region
        )
    }

    fun vedtak(sak: Sak, kodeRutine: String = "BA", kodeResultat: String = "  "): Vedtak {
        return Vedtak(
            1L,
            1L,
            sak.saksnummer.toLong(),
            sak.saksblokk,
            1L,
            kodeRutine,
            kodeResultat,
            listOf(Delytelse(1L, LocalDate.now(), null, 1940.0, "MS", typeUtbetaling = "M"))
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
    ): Utbetaling {
        return Utbetaling(
            personKey = stønad.personKey,
            startUtbetalingMåned = stønad.iverksattFom,
            virksomFom = stønad.virkningFom,
            kontonummer = kontonummer,
            utbetalingstype = "M",
            beløp = beløp,
            fnr = stønad.fnr,
            utbetalingId = nextId(),
            utbetalingTom = stønad.opphørtFom
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
            fnr = stønad.fnr,
        )
    }

    fun sakPerson(person: Person): SakPerson {
        return SakPerson(nextId(), person.region, person.personKey, person.fnr)
    }

    private val fnrGenerator = FoedselsnummerGenerator()
}
