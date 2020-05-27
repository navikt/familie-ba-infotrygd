package no.nav.infotrygd.barnetrygd.dto

fun periodeToSakDto(periode: no.nav.infotrygd.barnetrygd.model.Periode): SakDto {

    return SakDto(
        sakId = null,
        tema = periode.stoenadstype?.tema?.toDto(),
        behandlingstema = periode.stoenadstype?.toDto(),
        type = null,
        status = periode.frisk.status?.toDto(),
        resultat = null,
        vedtatt = null,
        iverksatt = periode.arbufoer,
        registrert = periode.registrert,
        opphoerFom = periode.opphoerFom
    )
}