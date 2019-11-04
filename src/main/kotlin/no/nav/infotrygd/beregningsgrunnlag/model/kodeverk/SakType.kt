package no.nav.infotrygd.beregningsgrunnlag.model.kodeverk

enum class SakType(override val tekst: String) : Kode {
    S("Søknad"),
    R("Revurdering"),
    K("Klage"),
    A("Anke");
    override val kode: String
        get() = this.name
}