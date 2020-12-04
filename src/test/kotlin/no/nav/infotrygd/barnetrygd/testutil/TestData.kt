package no.nav.infotrygd.barnetrygd.testutil

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.commons.foedselsnummer.Kjoenn
import no.nav.commons.foedselsnummer.testutils.FoedselsnummerGenerator
import no.nav.infotrygd.barnetrygd.model.*
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
        return Sak(
            idSak = nextId(),
            s01Personkey = person.personKey,
            mottattdato = LocalDate.now(),
            regDato = LocalDate.now(),
            fNr = person.fnr,
            region = person.region,
        )
    }

    private val fnrGenerator = FoedselsnummerGenerator()
}