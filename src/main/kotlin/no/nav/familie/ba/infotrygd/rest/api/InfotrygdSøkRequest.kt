package no.nav.familie.ba.infotrygd.rest.api

import no.nav.commons.foedselsnummer.FoedselsNr

data class InfotrygdSøkRequest(
    val brukere: List<String>,
    val barn: List<String>? = null,
)
