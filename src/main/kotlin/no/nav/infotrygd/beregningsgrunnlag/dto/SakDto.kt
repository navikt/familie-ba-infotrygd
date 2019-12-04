package no.nav.infotrygd.beregningsgrunnlag.dto

import java.time.LocalDate

data class SakDto(
    val sakId: String?,
    val tema: Kodeverdi?,
    val behandlingstema: Kodeverdi?,
    val type: Kodeverdi?,
    val status: Kodeverdi?,
    val resultat: Kodeverdi?,
    val vedtatt: LocalDate?,
    val iverksatt: LocalDate?,
    val opphoerFom: LocalDate?
)