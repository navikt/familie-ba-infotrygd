package no.nav.infotrygd.beregningsgrunnlag.model.kodeverk

import no.nav.infotrygd.beregningsgrunnlag.dto.Kodeverdi

enum class Stoenadstype(override val kode: String, override val tekst: String) : Kode { // todo: termnavn
    // Sykepenger
    SYKEPENGER("", "Sykepenger"),

    // Foreldrepenger
    FOEDSEL("FP", "Foreldrepenger m/ fødsel"),
    ADOPSJON("AP", "Foreldrepenger m/ adopsjon"),
    RISIKOFYLT_ARBMILJOE("SV", "Svangerskapspenger"),
    SVANGERSKAP("Z", "Svangerskapspenger"),

    // Pårørendesykdom
    BARNS_SYKDOM("OM", "Omsorgspenger"),
    ALV_SYKT_BARN("PB", "Pleiepenger sykt barn (identdato før 1.10.2017)"),
    KURS_KAP_3_23("OP", "Opplæringspenger"),
    PAS_DOEDSSYK("PP", "Pleiepenger pårørende"),
    PLEIEPENGER_INSTOPPH("PI", "Pleiepenger (identdato før 1.10.2017)"),
    PLEIEPENGER_NY_ORDNING("PN", "Pleiepenger, ny ordning (identdato etter 1.10.2017)"); // ???

    fun toBehandlingstema(): Kodeverdi {
        return when(this) {
            SYKEPENGER -> Kodeverdi("SP", tekst)
            FOEDSEL -> Kodeverdi("FØ", tekst)
            SVANGERSKAP -> Kodeverdi("SV", "Svangerskapspenger")
            else -> Kodeverdi(kode, tekst)
        }
    }
}