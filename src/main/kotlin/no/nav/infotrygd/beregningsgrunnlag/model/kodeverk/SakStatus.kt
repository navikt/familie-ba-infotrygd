package no.nav.infotrygd.beregningsgrunnlag.model.kodeverk

enum class SakStatus(override val tekst: String) : Kode {
    IKKE_BEHANDLET("Ikke behandlet") {
        override val kode: String get() = " "
    },

    IP("Saksbehandlingen kan starte med Statuskode IP (Ikke påbegynt). Da er det kun registrert en sakslinje uten at vedtaksbehandling er startet."),
    UB("Saksbehandling startet - når sak med status UB - Under Behandling - lagres, rapporteres hendelsen BehandlingOpprettet"),
    SG("Saksbehandler 1 har fullført og sendt til saksbehandler 2 for godkjenning"),
    UK("Underkjent av saksbehandler 2 med retur til saksbehandler 1"),
    FB("FerdigBehandlet"),
    FI("ferdig iverksatt"),
    RF("returnert feilsendt"),
    RM("returnert midlertidig"),
    RT("returnert til"),
    ST("sendt til"),
    VD("videresendt Direktoratet"),
    VI("venter på iverksetting"),
    VT("videresendt Trygderetten");

    override val kode: String
        get() = this.name
}