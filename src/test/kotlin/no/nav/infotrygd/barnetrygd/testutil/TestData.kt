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
        barnFnr: FoedselsNr = foedselsNr()
    ): Barn {
        return Barn(
            id = nextId(),
            fnr = person.fnr,
            tkNr = person.tkNr,
            personKey = person.personKey,
            barnFnr = barnFnr,
            barnetrygdTom = barnetrygdTom
        )
    }

    fun person(
        fnr: FoedselsNr = foedselsNr(),
        tkNr: String = "1000",
        personKey: Long = tkNr.let { it + fnr.asString }.toLong(),
        stønader: List<Stønad> = listOf(),
        barn: List<Barn> = listOf()
    ): Person {
        return Person(
            id = nextId(),
            fnr = fnr,
            tkNr = tkNr,
            personKey = personKey,
            stønader = stønader,
            barn = barn
        )
    }

    fun stønad(
        mottaker: Person,
        opphørtFom: String = "000000"
    ): Stønad {
        return Stønad(
            id = nextId(),
            personKey = mottaker.personKey,
            fnr = mottaker.fnr,
            tkNr = mottaker.tkNr,
            opphørtFom = opphørtFom
        )
    }

    private val fnrGenerator = FoedselsnummerGenerator()
}