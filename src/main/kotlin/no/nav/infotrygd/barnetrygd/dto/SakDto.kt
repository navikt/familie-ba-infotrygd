package no.nav.infotrygd.barnetrygd.dto

import java.time.LocalDate

data class SakResult(
    val saker: List<SakDto>,
    val vedtak: List<SakDto>
)

data class SakDto(
    val sakId: String?,
    val tema: Kodeverdi?,
    val behandlingstema: Kodeverdi?,
    val type: Kodeverdi?,
    val status: Kodeverdi?,
    val resultat: Kodeverdi?,
    val vedtatt: LocalDate?,
    val iverksatt: LocalDate?,
    val registrert: LocalDate?,
    val opphoerFom: LocalDate?
)