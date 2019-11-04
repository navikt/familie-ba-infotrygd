package no.nav.infotrygd.beregningsgrunnlag.model.kodeverk

enum class Frisk(val status: FriskStatus?, val kode: String) {
    LOPENDE(FriskStatus.LOEPENDE, " "),
    AVVIST(FriskStatus.IKKE_STARTET, "A"),
    BARN(FriskStatus.LOEPENDE, "B"),
    DOEDSSYK(FriskStatus.LOEPENDE, "D"),
    EGENMELDING(FriskStatus.LOEPENDE, "E"),
    FRISKMELDT(FriskStatus.AVSLUTTET, "F"),
    HISTORIKK(null, "H"),
    PASSIV(FriskStatus.IKKE_STARTET, "P"),
    TILBAKEKJOERT(FriskStatus.AVSLUTTET, "T")
}