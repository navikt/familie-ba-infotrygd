package no.nav.infotrygd.beregningsgrunnlag.model.kodeverk

enum class FriskStatus(override val kode: String, override val tekst: String) : Kode {
    LOEPENDE("L", "Løpende"),
    AVSLUTTET("A", "Avsluttet"),
    IKKE_STARTET("I", "Ikke startet")
}