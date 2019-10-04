package no.nav.infotrygd.beregningsgrunnlag.model.kodeverk

import no.nav.infotrygd.beregningsgrunnlag.dto.Kodeverdi

interface Kode {
    val kode: String
    val tekst: String

    fun toDto(): Kodeverdi = Kodeverdi(kode, tekst)
}