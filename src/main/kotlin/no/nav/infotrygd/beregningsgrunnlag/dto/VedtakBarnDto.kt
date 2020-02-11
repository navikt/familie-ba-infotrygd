package no.nav.infotrygd.beregningsgrunnlag.dto

data class VedtakBarnDto(
    val soekerFnr: String,
    val vedtak: List<SakDto>
)