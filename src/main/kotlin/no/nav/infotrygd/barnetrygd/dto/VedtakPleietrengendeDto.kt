package no.nav.infotrygd.barnetrygd.dto

import io.swagger.annotations.ApiModelProperty

data class VedtakPleietrengendeDto(
    @ApiModelProperty("Søkers fødselsnummer")
    val soekerFnr: String,

    @ApiModelProperty("Vedtak for pleietrengende som tilhører denne søkeren")
    val vedtak: List<SakDto>
)