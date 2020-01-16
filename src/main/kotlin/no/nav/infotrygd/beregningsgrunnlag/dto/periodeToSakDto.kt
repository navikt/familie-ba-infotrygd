package no.nav.infotrygd.beregningsgrunnlag.dto

fun periodeToSakDto(periode: no.nav.infotrygd.beregningsgrunnlag.model.Periode): SakDto {

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