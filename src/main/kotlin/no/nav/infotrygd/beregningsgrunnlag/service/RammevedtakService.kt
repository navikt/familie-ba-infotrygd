package no.nav.infotrygd.beregningsgrunnlag.service

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.infotrygd.beregningsgrunnlag.dto.RammevedtakDto
import no.nav.infotrygd.beregningsgrunnlag.dto.personkortToRammevedtakDto
import no.nav.infotrygd.beregningsgrunnlag.repository.PersonkortRepository
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