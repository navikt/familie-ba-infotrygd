package no.nav.infotrygd.beregningsgrunnlag.service

import no.nav.infotrygd.beregningsgrunnlag.dto.Svangerskapspenger
import no.nav.infotrygd.beregningsgrunnlag.dto.periodeToForeldrepengerDetaljer
import no.nav.infotrygd.beregningsgrunnlag.dto.periodeToGrunnlag
import no.nav.infotrygd.beregningsgrunnlag.dto.periodeToSvangerskapspengerDetaljer
import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Stoenadstype
import no.nav.infotrygd.beregningsgrunnlag.repository.PeriodeRepository
import no.nav.infotrygd.beregningsgrunnlag.repository.VedtakBarnRepository
import no.nav.infotrygd.beregningsgrunnlag.values.FodselNr
import org.springframework.stereotype.Service
import java.time.LocalDate
import javax.transaction.Transactional

@Service
@Transactional
class SvangerskapspengerService(
    private val periodeRepository: PeriodeRepository,
    private val vedtakBarnRepository: VedtakBarnRepository
) {
    fun hentSvangerskapspenger(fodselNr: FodselNr, fom: LocalDate, tom: LocalDate?): List<Svangerskapspenger> {

        val stoenadstyper = listOf(Stoenadstype.SVANGERSKAP, Stoenadstype.RISIKOFYLT_ARBMILJOE)

        val result = if(tom != null) {
            periodeRepository.findByFnrAndStoenadstypeAndDates(fodselNr, stoenadstyper, fom, tom)
        } else {
            periodeRepository.findByFnrAndStoenadstypeAndDates(fodselNr, stoenadstyper, fom)
        }

        return result.map { periode ->
            val vedtak = vedtakBarnRepository.findByPersonKeyAndArbufoerSeqAndKode(periode.personKey,
                periode.arbufoerSeq.toString(), periode.barnKode)
            Svangerskapspenger(
                generelt = periodeToGrunnlag(periode),
                foreldrepengerDetaljer = periodeToForeldrepengerDetaljer(periode, vedtak),
                svangerskapspengerDetaljer = periodeToSvangerskapspengerDetaljer(periode)
            )
        }
    }
}