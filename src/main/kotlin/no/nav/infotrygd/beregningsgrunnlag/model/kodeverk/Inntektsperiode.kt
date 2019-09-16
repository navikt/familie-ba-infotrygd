package no.nav.infotrygd.beregningsgrunnlag.model.kodeverk

enum class Inntektsperiode(val uri: String) {
    DAGLIG(""),
    UKENTLIG(""),
    DAGLIG_14(""),
    MAANEDLIG(""),
    AARLIG(""),
    INNTEKT_FASTSATT_ETTER_25_PROSENT_AVVIK(""),
    PREMIEGRUNNLAG_OPPDRAGSTAKER("")
}