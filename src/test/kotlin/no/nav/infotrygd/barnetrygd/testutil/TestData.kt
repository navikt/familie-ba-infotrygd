package no.nav.infotrygd.barnetrygd.testutil

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.commons.foedselsnummer.Kjoenn
import no.nav.commons.foedselsnummer.testutils.FoedselsnummerGenerator
import no.nav.infotrygd.barnetrygd.model.*
import no.nav.infotrygd.barnetrygd.model.kodeverk.SakStatus
import no.nav.infotrygd.barnetrygd.nextId
import java.time.LocalDate

object TestData {
    fun foedselsNr(
        foedselsdato: LocalDate? = null,
        kjoenn: Kjoenn = Kjoenn.MANN): FoedselsNr {

        return fnrGenerator.foedselsnummer(
            foedselsdato = foedselsdato,
            kjoenn = kjoenn
        )
    }

    fun barn(
        person: Person,
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
            region = region
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

    fun stønad(
        mottaker: Person,
        opphørtFom: String = "000000",
        region: String = "X"
    ): Stønad {
        return Stønad(
            id = nextId(),
            personKey = mottaker.personKey,
            fnr = mottaker.fnr,
            tkNr = mottaker.tkNr,
            opphørtFom = opphørtFom,
            region = region
        )
    }

    fun sak(person: Person): Sak {
        val saksblokk = "A"
        val saksnummer = "01"
        val region = "2"
        return Sak(
            id = nextId(),
            personKey = person.personKey,
            person = Sak.Person(nextId(), person.region, person.personKey, person.fnr),
            saksblokk = saksblokk,
            saksnummer = saksnummer,
            mottattdato = LocalDate.now(),
            regDato = LocalDate.now(),
            region = person.region,
            kapittelNr = "BA",
            valg = "OR",
            type = "S",
            resultat = "I",
            vedtaksdato = LocalDate.now(),
            iverksattdato = LocalDate.now(),
            statushistorikk = listOf(
                Status(
                    id = nextId(),
                    region = region,
                    personKey = person.personKey,
                    saksblokk = saksblokk,
                    saksnummer = saksnummer,
                    lopeNr = nextId() % 99,
                    status = SakStatus.IKKE_BEHANDLET
                )
            )
        )
    }

    private val fnrGenerator = FoedselsnummerGenerator()
}