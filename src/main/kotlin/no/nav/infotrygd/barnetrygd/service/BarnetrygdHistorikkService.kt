package no.nav.infotrygd.barnetrygd.service

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.infotrygd.barnetrygd.repository.BarnRepository
import no.nav.infotrygd.barnetrygd.repository.PersonRepository
import org.springframework.stereotype.Service

@Service
class BarnetrygdHistorikkService(
    private val personRepository: PersonRepository,
    private val barnRepository: BarnRepository
) {
    fun finnes(brukerFnr: List<FoedselsNr>, barnFnr: List<FoedselsNr>?): Boolean {
        val personFinnes = personRepository.findByFnrList(brukerFnr).isNotEmpty()
        val barnFinnes = barnFnr?.let { barnRepository.findByFnrList(it) }?.isNotEmpty() == true
        return personFinnes || barnFinnes
    }
}