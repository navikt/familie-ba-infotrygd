package no.nav.infotrygd.barnetrygd.rest.api

import io.swagger.annotations.ApiModelProperty
import no.nav.infotrygd.barnetrygd.model.Sak
import java.time.LocalDate

data class SakResult(
    val bruker: List<SakDto>,
    val barn: List<SakDto>,
)

data class SakDto(
    val s01Personkey: Long? = null,
    val s05Saksblokk: String? = null,
    val saksnr: String? = null,
    val regDato: LocalDate? = null,
    val mottattdato: LocalDate? = null,
    val kapittelnr: String? = null,
    val valg: String? = null,
    val dublettFeil: String? = null,
    val type: String? = null,
    val innstilling: String? = null,
    val resultat: String? = null,
    val vedtaksdato: LocalDate? = null,
    val vedtak: StønadDto? = null,
    val nivå: String? = null,
    val innstilldato: LocalDate? = null,
    val iverksattdato: LocalDate? = null,
    val grunnblDato: LocalDate? = null,
    val årsakskode: String? = null,
    val tellepunkt: String? = null,
    val telletype: String? = null,
    val telledato: LocalDate? = null,
    val evalKode: String? = null,
    val evalTir: String? = null,
    val fremlegg: String? = null,
    val innstilling2: String? = null,
    val innstilldato2: LocalDate? = null,
    val annenInstans: String? = null,
    val behenType: String? = null,
    val behenEnhet: String? = null,
    val regAvType: String? = null,
    val regAvEnhet: String? = null,
    val diffFramlegg: String? = null,
    val innstilltAvType: String? = null,
    val innstilltAvEnhet: String? = null,
    val vedtattAvType: String? = null,
    val vedtattAvEnhet: String? = null,
    val prioTab: String? = null,
    val aoe: String? = null,
    val esSystem: String? = null,
    val esGsakOppdragsid: Long? = null,
    val knyttetTilSak: String? = null,
    val vedtakstype: String? = null,
    val reellEnhet: String? = null,
    val modEndret: String? = null,
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
        dublettFeil = this.dublettFeil,
        type = this.type,
        innstilling = this.innstilling,
        resultat = this.resultat,
        vedtaksdato = this.vedtaksdato,
        vedtak = this.vedtak?.toStønadDto(),
        nivå = this.nivaa,
        innstilldato = this.innstilldato,
        iverksattdato = this.iverksattdato,
        grunnblDato = this.grunnblDato,
        årsakskode = this.aarsakskode,
        tellepunkt = this.tellepunkt,
        telletype = this.telletype,
        telledato = this.telledato,
        evalKode = this.evalKode,
        evalTir = this.evalTir,
        fremlegg = this.fremlegg,
        innstilling2 = this.innstilling2,
        innstilldato2 = this.innstilldato2,
        annenInstans = this.annenInstans,
        behenType = this.behenType,
        behenEnhet = this.behenEnhet,
        regAvType = this.regAvType,
        regAvEnhet = this.regAvEnhet,
        diffFramlegg = this.diffFramlegg,
        innstilltAvType = this.innstilltAvType,
        innstilltAvEnhet = this.innstilltAvEnhet,
        vedtattAvType = this.vedtattAvType,
        vedtattAvEnhet = this.vedtattAvEnhet,
        prioTab = this.prioTab,
        aoe = this.aoe,
        esSystem = this.esSystem,
        esGsakOppdragsid = this.esGsakOppdragsid,
        knyttetTilSak = this.knyttetTilSak,
        vedtakstype = this.vedtakstype,
        reellEnhet = this.reellEnhet,
        modEndret = this.modEndret,
        tkNr = this.tkNr,
        fNr = this.person.fnr.asString,
        kildeIs = this.kildeIs,
        region = this.region,
        sakId = this.id,
        status = this.status.kode
    )
}