package no.nav.infotrygd.barnetrygd.service

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.infotrygd.barnetrygd.dto.RammevedtakDto
import no.nav.infotrygd.barnetrygd.dto.personkortToRammevedtakDto
import no.nav.infotrygd.barnetrygd.repository.PersonkortRepository
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class RammevedtakService(private val personkortRepository: PersonkortRepository) {
    companion object {
        const val KONTONUMMER_OM: Long = 2920001
    }

    fun hentRammevedtak(kontonr: Long, fnr: FoedselsNr, fom: LocalDate, tom: LocalDate?): List<RammevedtakDto> {
        return personkortRepository.findByKontonummerAndFnr(kontonr, fnr)
            .filter { it.innenforPeriode(fom, tom ?: LocalDate.MAX) }
            .map(::personkortToRammevedtakDto)
    }
}