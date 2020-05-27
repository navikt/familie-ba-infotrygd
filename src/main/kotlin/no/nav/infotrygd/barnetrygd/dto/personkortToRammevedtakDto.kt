package no.nav.infotrygd.barnetrygd.dto

import no.nav.infotrygd.barnetrygd.model.ip.Personkort

fun personkortToRammevedtakDto(personkort: Personkort): RammevedtakDto {
    return RammevedtakDto(
        tekst = personkort.tekst.trim(),
        fom = personkort.fom,
        tom = personkort.tom,
        date = personkort.dato
    )
}