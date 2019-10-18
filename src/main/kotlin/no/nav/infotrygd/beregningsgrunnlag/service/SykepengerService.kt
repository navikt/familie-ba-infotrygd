package no.nav.infotrygd.beregningsgrunnlag.service

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.infotrygd.beregningsgrunnlag.dto.Sykepenger
import no.nav.infotrygd.beregningsgrunnlag.dto.periodeToGrunnlag
import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Stoenadstype
import no.nav.infotrygd.beregningsgrunnlag.repository.PeriodeRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import javax.transaction.Transactional

@Service
@Transactional
class SykepengerService(private val periodeRepository: PeriodeRepository) {
    fun hentSykepenger(foedselsNr: FoedselsNr, fom: LocalDate, tom: LocalDate?): List<Sykepenger> {
        val stoenadstyper = listOf(Stoenadstype.SYKEPENGER)

        val perioder = if(tom == null) {
            periodeRepository.findByFnrAndStoenadstypeAndDates(foedselsNr, stoenadstyper, fom)
        } else {
            periodeRepository.findByFnrAndStoenadstypeAndDates(foedselsNr, stoenadstyper, fom, tom)
        }

        return perioder.map {
            Sykepenger(
                generelt = periodeToGrunnlag(it),
                inntektsgrunnlagProsent = it.inntektsgrunnlagProsent
            )
        }
    }
}