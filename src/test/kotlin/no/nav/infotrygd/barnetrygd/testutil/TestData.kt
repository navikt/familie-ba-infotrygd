package no.nav.infotrygd.barnetrygd.testutil

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.commons.foedselsnummer.Kjoenn
import no.nav.commons.foedselsnummer.testutils.FoedselsnummerGenerator
import no.nav.infotrygd.barnetrygd.model.db2.Delytelse
import no.nav.infotrygd.barnetrygd.model.db2.Utbetaling
import no.nav.infotrygd.barnetrygd.model.db2.Vedtak
import no.nav.infotrygd.barnetrygd.model.dl1.Barn
import no.nav.infotrygd.barnetrygd.model.dl1.Person
import no.nav.infotrygd.barnetrygd.model.dl1.Sak
import no.nav.infotrygd.barnetrygd.model.dl1.Status
import no.nav.infotrygd.barnetrygd.model.dl1.Stønad
import no.nav.infotrygd.barnetrygd.model.kodeverk.SakStatus
import no.nav.infotrygd.barnetrygd.nextId
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
        iverksatt: String = "010101",
        virkningFom: String = "010101",
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
        barn: List<Barn>? = null,
        status: String = "01",
        iverksattFom: String = (999999-202005).toString(),
        virkningFom: String = (999999-202005).toString(),
        opphørtIver: String = "000000",
        opphørtFom: String = "000000",
        opphørsgrunn: String? = "M",
        region: String = "X",
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
            region = region,
            barn = barn ?: listOf(barn(mottaker, iverksattFom, virkningFom)),
        )
    }

    fun sak(person: Person, stønad: Stønad? = null,
            valg: String = "OR",
            undervalg: String = "OS"): Sak {
        val saksblokk = "A"
        val saksnummer = "01"
        val region = "2"

        return Sak(
            id = nextId(),
            personKey = person.personKey,
            person = Sak.Person(nextId(), person.region, person.personKey, person.fnr),
            saksblokk = stønad?.saksblokk ?: saksblokk,
            saksnummer = stønad?.sakNr ?: saksnummer,
            mottattdato = LocalDate.now(),
            regDato = LocalDate.now(),
            region = person.region,
            kapittelNr = "BA",
            valg = valg,
            undervalg = undervalg,
            type = "S",
            resultat = "I",
            stønadList = stønad?.let { listOf(it) } ?: emptyList(),
            vedtaksdato = LocalDate.now(),
            iverksattdato = LocalDate.now(),
            statushistorikk = listOf(
                Status(
                    id = nextId(),
                    region = region,
                    personKey = person.personKey,
                    saksblokk = stønad?.saksblokk ?: saksblokk,
                    saksnummer = stønad?.sakNr ?: saksnummer,
                    lopeNr = nextId() % 99,
                    status = SakStatus.IKKE_BEHANDLET
                )
            )
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

    private val fnrGenerator = FoedselsnummerGenerator()
}
