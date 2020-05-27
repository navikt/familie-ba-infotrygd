package no.nav.infotrygd.barnetrygd.model.kodeverk

enum class SakResultat(override val kode: String, override val tekst: String) : Kode {
    BESLUTNINGSSTOETTE("?", "beslutningsstøtte Besl st"),
    A("A", " Avslag"),
    AK("AK", "avvist klage"),
    AV("AV", "advarsel"),
    DI("DI", "delvis innvilget"),
    DT("DT", "delvis tilbakebetale"),
    FB("FB", "ferdigbehandlet"),
    FI("FI", "fortsatt innvilget"),
    H("H", "henlagt / trukket tilbake"),
    HB("HB", "henlagt / bortfalt"),
    I("I", "Innvilget"),
    IN("IN", "innvilget ny situasjon"),
    IS("IS", "ikke straffbart"),
    IT("IT", " ikke tilbakebetale"),
    MO("MO", "midlertidig opphørt"),
    MT("MT", "mottatt"),
    O("O", "opphørt"),
    PA("PA", "politianmeldelse"),
    R("R", "redusert"),
    SB("SB", "sak i bero"),
    TB("TB", "tilbakebetale"),
    TH("TH", "tips henlagt"),
    TO("TO", "tips oppfølging"),
    OEKNING("Ø", "økning")
}