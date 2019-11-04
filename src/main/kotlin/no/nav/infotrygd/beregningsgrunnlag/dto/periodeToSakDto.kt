package no.nav.infotrygd.beregningsgrunnlag.dto

fun periodeToSakDto(periode: no.nav.infotrygd.beregningsgrunnlag.model.Periode): SakDto {

    val opphoerFom = periode.stoppdato
        ?: periode.friskmeldtDato
        ?: periode.arbufoerTom?.plusDays(1)
        ?: periode.maksdato

    return SakDto(
        sakId = null,
        tema = periode.stoenadstype?.tema?.toDto(),
        behandlingstema = periode.stoenadstype?.toDto(),
        type = null,
        status = periode.frisk.status?.toDto(),
        resultat = null,
        vedtatt = null,
        iverksatt = periode.arbufoer,
        opphoerFom = opphoerFom
    )
}