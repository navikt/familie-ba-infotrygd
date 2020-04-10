package no.nav.infotrygd.beregningsgrunnlag.dto

import java.time.LocalDate

data class RammevedtakDto(
    val tekst:String?,
    val fom:LocalDate?,
    val tom:LocalDate?,
    val date: LocalDate?
)