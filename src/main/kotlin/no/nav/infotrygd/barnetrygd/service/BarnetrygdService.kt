package no.nav.infotrygd.barnetrygd.service

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.infotrygd.barnetrygd.repository.BarnRepository
import no.nav.infotrygd.barnetrygd.repository.PersonRepository
import no.nav.infotrygd.barnetrygd.repository.StønadRepository
import org.springframework.stereotype.Service

@Service
class BarnetrygdService(
    private val personRepository: PersonRepository,
    private val stonadRepository: StønadRepository,
    private val barnRepository: BarnRepository
) {

    fun finnes(brukerFnr: List<FoedselsNr>, barnFnr: List<FoedselsNr>?): Boolean {
        val personFinnes = personRepository.findByFnrList(brukerFnr).isNotEmpty()
        val barnFinnes = barnFnr?.let { barnRepository.findByFnrList(it) }?.isNotEmpty() == true
        return personFinnes || barnFinnes
    }

    fun mottarBarnetrygd(brukerFnr: List<FoedselsNr>, barnFnr: List<FoedselsNr>?): Boolean {
        val personMottarBarnetrygd = personRepository.findByFnrList(brukerFnr)
            .flatMap { stonadRepository.findByPersonKeyAndRegion(it.personKey, it.region) }
            .isNotEmpty()
        val mottasBarnetrygdForBarn = barnFnr?.let {
            barnRepository.findActiveByFnrList(it)
                .flatMap { stonadRepository.findByPersonKeyAndRegion(it.personKey, it.region) }
        }?.isNotEmpty() == true

        return personMottarBarnetrygd || mottasBarnetrygdForBarn
    }
}