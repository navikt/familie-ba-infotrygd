package no.nav.infotrygd.beregningsgrunnlag.dto

import no.nav.infotrygd.beregningsgrunnlag.model.ip.Personkort

fun personkortToRammevedtakDto(personkort: Personkort): RammevedtakDto {
    return RammevedtakDto(
        tekst = personkort.tekst.trim(),
        fom = personkort.fom,
        tom = personkort.tom,
        date = personkort.dato
    )
}