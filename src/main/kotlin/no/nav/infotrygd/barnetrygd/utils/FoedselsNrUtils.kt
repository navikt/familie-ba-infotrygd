package no.nav.infotrygd.barnetrygd.utils

import no.nav.commons.foedselsnummer.FoedselsNr


val FoedselsNr.reversert: String
    get() {
        return reverse(asString)
    }

fun FoedselsNr.Companion.fraReversert(reversert: String): FoedselsNr {
    return FoedselsNr(reverse(reversert))
}

private val regex = """(\d\d)(\d\d)(\d\d)(\d{5})""".toRegex()

private fun reverse(fnr: String): String {
    require(regex.matches(fnr)) { "Ikke et gyldig (reversert?) fødselsnummer: $fnr" }

    val (a, b, c, pnr) = regex.find(fnr)!!.destructured
    return "$c$b$a$pnr"
}