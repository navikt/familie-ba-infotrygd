package no.nav.infotrygd.barnetrygd.model.kodeverk

enum class Tema(override val kode: String, override val tekst: String) : Kode {
    SYKEPENGER("SP", "Sykepenger"),
    FORELDREPENGER("FA", "Foreldrepenger"),
    PAAROERENDE_SYKDOM("BS", "Barns sykdom"),
    UKJENT("ukjent", "ukjent");

    val stoenadstyper: List<Stoenadstype>
        get() {
            return Stoenadstype.values().filter { it.tema == this }
        }
}