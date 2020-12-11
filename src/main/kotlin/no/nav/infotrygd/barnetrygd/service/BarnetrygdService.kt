package no.nav.infotrygd.barnetrygd.service

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.infotrygd.barnetrygd.repository.BarnRepository
import no.nav.infotrygd.barnetrygd.repository.PersonRepository
import no.nav.infotrygd.barnetrygd.repository.SakRepository
import no.nav.infotrygd.barnetrygd.repository.StønadRepository
import no.nav.infotrygd.barnetrygd.rest.api.SakDto
import no.nav.infotrygd.barnetrygd.rest.api.StønadDto
import no.nav.infotrygd.barnetrygd.rest.api.toSakDto
import no.nav.infotrygd.barnetrygd.rest.api.toStønadDto
import org.springframework.stereotype.Service

@Service
class BarnetrygdService(
    private val personRepository: PersonRepository,
    private val stonadRepository: StønadRepository,
    private val barnRepository: BarnRepository,
    private val sakRepository: SakRepository,
) {

    @Deprecated("Controller-metoden som benytter den er deprecated.")
    fun mottarBarnetrygd(brukerFnr: List<FoedselsNr>, barnFnr: List<FoedselsNr>?): Boolean {
        val personMottarBarnetrygd = brukerFnr.isNotEmpty() && personRepository.findByFnrList(brukerFnr)
            .flatMap { stonadRepository.findByPersonKeyAndRegion(it.personKey, it.region) }
            .isNotEmpty()
        val mottasBarnetrygdForBarn = barnFnr?.let {
            barnRepository.findBarnetrygdBarnInFnrList(it)
                .flatMap { stonadRepository.findByPersonKeyAndRegion(it.personKey, it.region) }
        }?.isNotEmpty() == true

        return personMottarBarnetrygd || mottasBarnetrygdForBarn
    }

    fun findLøpendeStønadByBrukerFnr(brukerFnr: List<FoedselsNr>): List<StønadDto> {
        return if (brukerFnr.isEmpty()) emptyList() else stonadRepository.findLøpendeStønadByFnr(brukerFnr).distinct()
            .map { it.toStønadDto() }
    }

    fun findLøpendeStønadByBarnFnr(barnFnr: List<FoedselsNr>): List<StønadDto>  {
        return if (barnFnr.isEmpty()) emptyList() else stonadRepository.findLøpendeStønadByBarnFnr(barnFnr).distinct()
            .map { it.toStønadDto() }
    }

    fun findSakerByBrukerFnr(brukerFnr: List<FoedselsNr>): List<SakDto> {
        return brukerFnr.flatMap { sakRepository.findBarnetrygdsakerByFnr(it) }.distinct()
            .map { it.toSakDto() }
    }

    fun findSakerByBarnFnr(barnFnr: List<FoedselsNr>): List<SakDto> {
        return if (barnFnr.isEmpty()) emptyList() else sakRepository.findBarnetrygdsakerByBarnFnr(barnFnr).distinct()
            .map { it.toSakDto() }
    }
}