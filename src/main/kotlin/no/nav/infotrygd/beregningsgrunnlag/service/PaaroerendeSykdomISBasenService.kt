package no.nav.infotrygd.beregningsgrunnlag.service

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.infotrygd.beregningsgrunnlag.dto.PaaroerendeSykdom
import no.nav.infotrygd.beregningsgrunnlag.dto.periodeToGrunnlag
import no.nav.infotrygd.beregningsgrunnlag.model.Periode
import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Stoenadstype.*
import no.nav.infotrygd.beregningsgrunnlag.repository.PeriodeRepository
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class PaaroerendeSykdomISBasenService(private val periodeRepository: PeriodeRepository) {
    fun hentPaaroerendeSykdom(foedselsNr: FoedselsNr, fom: LocalDate, tom: LocalDate?): List<PaaroerendeSykdom> {
        val stoenadstyper = listOf(
            BARNS_SYKDOM,
            ALV_SYKT_BARN,
            KURS_KAP_3_23,
            PAS_DOEDSSYK,
            PLEIEPENGER_INSTOPPH
        )

        val result = if(tom != null) {
            periodeRepository.findByFnrAndStoenadstypeAndDates(foedselsNr, stoenadstyper, fom, tom)
        } else {
            periodeRepository.findByFnrAndStoenadstypeAndDates(foedselsNr, stoenadstyper, fom)
        }

        return result.map { PaaroerendeSykdom(
            generelt = periodeToGrunnlag(it),
            foedselsdatoPleietrengende = foedselsdatoPleietrengende(it)
        ) }
    }
}

fun foedselsdatoPleietrengende(periode: Periode): LocalDate? {
    val stoenadstype = periode.stoenadstype
    return when(stoenadstype) {
        KURS_KAP_3_23, ALV_SYKT_BARN,PLEIEPENGER_INSTOPPH -> periode.morFnr?.foedselsdato
        BARNS_SYKDOM, PAS_DOEDSSYK -> periode.foedselsdatoBarn
        else -> throw IllegalStateException("Uventet stønadstype i IS-basen: ${stoenadstype?.kode}")
    }
}