package no.nav.infotrygd.barnetrygd.rest.api

import no.nav.commons.foedselsnummer.FoedselsNr

data class InfotrygdSøkRequest(val brukere: List<FoedselsNr>,
                               val barn: List<FoedselsNr>? = null)