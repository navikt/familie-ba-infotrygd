package no.nav.infotrygd.barnetrygd.model.kodeverk

enum class Inntektsperiode(override val kode: String, override val tekst: String) : Kode {
    DAGLIG("D", "Daglig"),
    UKENTLIG("U", "Ukentlig"),
    DAGLIG_14("F", "14-daglig"),
    MAANEDLIG("M", "Månedlig"),
    AARLIG("Å", "Årlig"),
    INNTEKT_FASTSATT_ETTER_25_PROSENT_AVVIK("X", "Inntekt fastsatt etter 25% avvik"),
    PREMIEGRUNNLAG_OPPDRAGSTAKER("Y", "Premiegrunnlag oppdragstaker (gjelder de 2 første ukene)")
}