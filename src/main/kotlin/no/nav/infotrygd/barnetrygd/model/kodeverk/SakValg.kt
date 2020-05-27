package no.nav.infotrygd.barnetrygd.model.kodeverk

enum class SakValg(override val kode: String, override val tekst: String) : Kode {
    OM("OM", "omsorgspenger"),
    OP("OP", "opplæringspenger"),
    PB("PB", "pleiepenger sykt barn"),
    PI("PI", "pleiepenger"),
    PN("PN", "pleiepenger ny ordning"),
    PP("PP", "pleiepenger pårørende"),

    UGYLDIG("~!", "Ugyldig")
}