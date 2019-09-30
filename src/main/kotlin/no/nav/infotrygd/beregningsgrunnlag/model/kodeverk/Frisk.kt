package no.nav.infotrygd.beregningsgrunnlag.model.kodeverk

enum class Frisk(val status: Status?, val kode: String) {
    LOPENDE(Status.LOEPENDE, " "),
    AVVIST(Status.IKKE_STARTET, "A"),
    BARN(Status.LOEPENDE, "B"),
    DOEDSSYK(Status.LOEPENDE, "D"),
    EGENMELDING(Status.LOEPENDE, "E"),
    FRISKMELDT(Status.AVSLUTTET, "F"),
    HISTORIKK(null, "H"),
    PASSIV(Status.IKKE_STARTET, "P"),
    TILBAKEKJOERT(Status.AVSLUTTET, "T")
}