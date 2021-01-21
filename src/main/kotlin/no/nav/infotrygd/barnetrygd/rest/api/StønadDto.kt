package no.nav.infotrygd.barnetrygd.rest.api

import no.nav.infotrygd.barnetrygd.model.Stønad


data class StønadResult(
    val bruker: List<StønadDto>,
    val barn: List<StønadDto>,
)

data class StønadDto(
    val stønadId: Long,
    val sakNr: String,
    val opphørtFom: String?,
    val opphørsgrunn: String? = null,
)

fun Stønad.toStønadDto(): StønadDto {
    return StønadDto(
        stønadId = this.id,
        sakNr = this.sakNr,
        opphørtFom = this.opphørtFom,
        opphørsgrunn = this.opphørsgrunn,
    )
}