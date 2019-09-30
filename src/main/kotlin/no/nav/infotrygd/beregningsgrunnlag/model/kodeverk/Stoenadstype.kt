package no.nav.infotrygd.beregningsgrunnlag.model.kodeverk

import no.nav.infotrygd.beregningsgrunnlag.dto.Kodeverdi

enum class Stoenadstype(val tema: Tema, override val kode: String, override val tekst: String) : Kode { // todo: termnavn
    // Sykepenger
    SYKEPENGER(Tema.SYKEPENGER,"", "Sykepenger"),

    // Foreldrepenger
    FOEDSEL(Tema.FORELDREPENGER,"FP", "Foreldrepenger m/ fødsel"),
    ADOPSJON(Tema.FORELDREPENGER,"AP", "Foreldrepenger m/ adopsjon"),
    RISIKOFYLT_ARBMILJOE(Tema.FORELDREPENGER,"SV", "Svangerskapspenger"),
    SVANGERSKAP(Tema.FORELDREPENGER,"Z", "Svangerskapspenger"),

    // Pårørendesykdom
    BARNS_SYKDOM(Tema.PAAROERENDE_SYKDOM,"OM", "Omsorgspenger"),
    ALV_SYKT_BARN(Tema.PAAROERENDE_SYKDOM,"PB", "Pleiepenger sykt barn (identdato før 1.10.2017)"),
    KURS_KAP_3_23(Tema.PAAROERENDE_SYKDOM,"OP", "Opplæringspenger"),
    PAS_DOEDSSYK(Tema.PAAROERENDE_SYKDOM,"PP", "Pleiepenger pårørende"),
    PLEIEPENGER_INSTOPPH(Tema.PAAROERENDE_SYKDOM,"PI", "Pleiepenger (identdato før 1.10.2017)"),
    PLEIEPENGER_NY_ORDNING(Tema.PAAROERENDE_SYKDOM,"PN", "Pleiepenger, ny ordning (identdato etter 1.10.2017)"); // ???

    fun toBehandlingstema(): Kodeverdi {
        return when(this) {
            SYKEPENGER -> Kodeverdi("SP", tekst)
            FOEDSEL -> Kodeverdi("FØ", tekst)
            SVANGERSKAP -> Kodeverdi("SV", "Svangerskapspenger")
            else -> Kodeverdi(kode, tekst)
        }
    }
}