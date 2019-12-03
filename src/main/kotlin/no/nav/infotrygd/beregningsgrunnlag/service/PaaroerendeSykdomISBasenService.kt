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
    fun hentPaaroerendeSykdom(foedselsNr: FoedselsNr): List<Periode> {
        val stoenadstyper = listOf(
            BARNS_SYKDOM,
            ALV_SYKT_BARN,
            KURS_KAP_3_23,
            PAS_DOEDSSYK,
            PLEIEPENGER_INSTOPPH
        )

        return periodeRepository.findByFnrAndStoenadstype(foedselsNr, stoenadstyper)
    }
}

fun periodeToPaaroerendeSykdom(periode: Periode): PaaroerendeSykdom {
    return PaaroerendeSykdom(
        generelt = periodeToGrunnlag(periode),
        foedselsdatoPleietrengende = foedselsdatoPleietrengende(periode),
        foedselsnummerPleietrengende = foedselsnummerPleietrengende(periode)?.asString
    )
}

fun foedselsnummerPleietrengende(periode: Periode): FoedselsNr? {
    val stoenadstype = periode.stoenadstype
    return when(stoenadstype) {
        KURS_KAP_3_23, ALV_SYKT_BARN,PLEIEPENGER_INSTOPPH -> periode.morFnr
        BARNS_SYKDOM, PAS_DOEDSSYK -> null
        else -> throw IllegalStateException("Uventet stønadstype i IS-basen: ${stoenadstype?.kode}")
    }
}

fun foedselsdatoPleietrengende(periode: Periode): LocalDate? {
    val stoenadstype = periode.stoenadstype
    return when(stoenadstype) {
        KURS_KAP_3_23, ALV_SYKT_BARN,PLEIEPENGER_INSTOPPH -> null
        BARNS_SYKDOM, PAS_DOEDSSYK -> periode.foedselsdatoBarn
        else -> throw IllegalStateException("Uventet stønadstype i IS-basen: ${stoenadstype?.kode}")
    }
}