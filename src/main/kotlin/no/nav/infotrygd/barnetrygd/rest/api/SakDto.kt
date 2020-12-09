package no.nav.infotrygd.barnetrygd.rest.api

import io.swagger.annotations.ApiModelProperty
import no.nav.infotrygd.barnetrygd.model.Sak
import java.time.LocalDate

data class SakDto(
    val s01Personkey: Long? = null,
    val s05Saksblokk: String? = null,
    val saksnr: String? = null,
    val regDato: LocalDate? = null,
    val mottattdato: LocalDate? = null,
    val kapittelnr: String? = null,
    val valg: String? = null,
    val Undervalg1: String? = null,
    val Undervalg2: String? = null,
    val DublettFeil: String? = null,
    val Type: String? = null,
    val Innstilling: String? = null,
    val Resultat: String? = null,
    val Nivaa: String? = null,
    val Innstilldato: LocalDate? = null,
    val Vedtaksdato: LocalDate? = null,
    val Iverksattdato: LocalDate? = null,
    val GrunnblDato: LocalDate? = null,
    val Aarsakskode: String? = null,
    val Tellepunkt: String? = null,
    val Telletype: String? = null,
    val Telledato: LocalDate? = null,
    val EvalKode: String? = null,
    val EvalTir: String? = null,
    val Fremlegg: String? = null,
    val Innstilling2: String? = null,
    val Innstilldato2: LocalDate? = null,
    val AnnenInstans: String? = null,
    val BehenType: String? = null,
    val BehenEnhet: String? = null,
    val RegAvType: String? = null,
    val RegAvEnhet: String? = null,
    val DiffFramlegg: String? = null,
    val InnstilltAvType: String? = null,
    val InnstilltAvEnhet: String? = null,
    val VedtattAvType: String? = null,
    val VedtattAvEnhet: String? = null,
    val PrioTab: String? = null,
    val Aoe: String? = null,
    val EsSystem: String? = null,
    val EsGsakOppdragsid: Long? = null,
    val KnyttetTilSak: String? = null,
    val Vedtakstype: String? = null,
    val ReellEnhet: String? = null,
    val ModEndret: String? = null,
    val tkNr: String? = null,
    val fNr: String? = null,
    val kildeIs: String? = null,
    val region: String? = null,
    val sakId: Long? = null,

    @ApiModelProperty(notes = """
        IP: - Saksbehandlingen kan starte med Statuskode IP (Ikke påbegynt). Da er det kun registrert en sakslinje uten at vedtaksbehandling er startet.
        UB: - Saksbehandling startet - når sak med status UB - Under Behandling - lagres, rapporteres hendelsen BehandlingOpprettet
        SG: - Saksbehandler 1 har fullført og sendt til saksbehandler 2 for godkjenning
        UK: - Underkjent av saksbehandler 2 med retur til saksbehandler 1
        FB: - FerdigBehandlet
        FI: - ferdig iverksatt
        RF: - returnert feilsendt
        RM: - returnert midlertidig
        RT: - returnert til
        ST: - sendt til
        VD: - videresendt Direktoratet
        VI: - venter på iverksetting
        VT: - videresendt Trygderetten
        
        Kolonne: S15_STATUS.
    """,
        allowableValues = "IP,UB,SG,UK,FB,FI,RF,RM,RT,ST,VD,VI,VT"
    )
    val status: String,         // S15_STATUS

)

fun Sak.toSakDto(): SakDto {
    return SakDto(
        s01Personkey = this.personKey,
        s05Saksblokk = this.saksblokk,
        saksnr = this.saksnummer,
        regDato = this.regDato,
        mottattdato = this.mottattdato,
        kapittelnr = this.kapittelNr,
        valg = this.valg,
        DublettFeil = this.dublettFeil,
        Type = this.type,
        Innstilling = this.innstilling,
        Resultat = this.resultat,
        Nivaa = this.nivaa,
        Innstilldato = this.innstilldato,
        Vedtaksdato = this.vedtaksdato,
        Iverksattdato = this.iverksattdato,
        GrunnblDato = this.grunnblDato,
        Aarsakskode = this.aarsakskode,
        Tellepunkt = this.tellepunkt,
        Telletype = this.telletype,
        Telledato = this.telledato,
        EvalKode = this.evalKode,
        EvalTir = this.evalTir,
        Fremlegg = this.fremlegg,
        Innstilling2 = this.innstilling2,
        Innstilldato2 = this.innstilldato2,
        AnnenInstans = this.annenInstans,
        BehenType = this.behenType,
        BehenEnhet = this.behenEnhet,
        RegAvType = this.regAvType,
        RegAvEnhet = this.regAvEnhet,
        DiffFramlegg = this.diffFramlegg,
        InnstilltAvType = this.innstilltAvType,
        InnstilltAvEnhet = this.innstilltAvEnhet,
        VedtattAvType = this.vedtattAvType,
        VedtattAvEnhet = this.vedtattAvEnhet,
        PrioTab = this.prioTab,
        Aoe = this.aoe,
        EsSystem = this.esSystem,
        EsGsakOppdragsid = this.esGsakOppdragsid,
        KnyttetTilSak = this.knyttetTilSak,
        Vedtakstype = this.vedtakstype,
        ReellEnhet = this.reellEnhet,
        ModEndret = this.modEndret,
        tkNr = this.tkNr,
        fNr = this.fnr.asString,
        kildeIs = this.kildeIs,
        region = this.region,
        sakId = this.id,
        status = this.status.kode
    )
}