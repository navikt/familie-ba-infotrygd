package no.nav.infotrygd.barnetrygd.rest.api

import no.nav.infotrygd.barnetrygd.model.Stønad


data class StønadResult(
    val bruker: List<StønadDto>,
    val barn: List<StønadDto>,
)

data class StønadDto(
    val id: Long,
    val fnr: String,
    val tkNr: String,
    val region: String,
    val opphørtFom: String?,
    val opphørsgrunn: String? = null,
)

fun Stønad.toStønadDto(): StønadDto {
    return StønadDto(
        id = this.id,
        fnr = this.fnr.asString,
        tkNr = this.tkNr,
        region = this.region,
        opphørtFom = this.opphørtFom,
        opphørsgrunn = null, // TODO legg til i entitet
    )
}