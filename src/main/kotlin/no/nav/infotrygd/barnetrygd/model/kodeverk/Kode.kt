package no.nav.infotrygd.barnetrygd.model.kodeverk

import no.nav.infotrygd.barnetrygd.dto.Kodeverdi

interface Kode {
    val kode: String
    val tekst: String

    fun toDto(): Kodeverdi = Kodeverdi(kode, tekst)
}