package no.nav.infotrygd.barnetrygd.rest.api

import io.swagger.annotations.ApiModelProperty
import no.nav.infotrygd.barnetrygd.model.Sak
import java.time.LocalDate

data class SakResult(
    val bruker: List<SakDto>,
    val barn: List<SakDto>,
)

data class SakDto(
    val saksnr: String? = null,
    val saksblokk: String? = null,
    val regDato: LocalDate? = null,
    val mottattdato: LocalDate? = null,
    val kapittelnr: String? = null,
    val valg: String? = null,
    val undervalg: String? = null,
    val type: String? = null,
    val nivå: String? = null,
    val resultat: String? = null,
    val vedtaksdato: LocalDate? = null,
    val iverksattdato: LocalDate? = null,
    val stønadList: List<StønadDto> = emptyList(),
    val årsakskode: String? = null,
    val behenEnhet: String? = null,
    val regAvEnhet: String? = null,

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
        saksnr = this.saksnummer,
        saksblokk = this.saksblokk,
        regDato = this.regDato,
        mottattdato = this.mottattdato,
        kapittelnr = this.kapittelNr,
        valg = this.valg,
        undervalg = this.undervalg,
        type = this.type,
        nivå = this.nivaa,
        resultat = this.resultat,
        vedtaksdato = this.vedtaksdato,
        iverksattdato = this.iverksattdato,
        stønadList = this.stønadList.distinct().map { it.toStønadDto() },
        årsakskode = this.aarsakskode,
        behenEnhet = this.behenEnhet,
        regAvEnhet = this.regAvEnhet,
        status = this.status.kode,
    )
}