package no.nav.infotrygd.beregningsgrunnlag.service

import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Stoenadstype
import no.nav.infotrygd.beregningsgrunnlag.repository.PeriodeRepository
import no.nav.infotrygd.beregningsgrunnlag.repository.VedtakBarnRepository
import no.nav.infotrygd.beregningsgrunnlag.dto.*
import no.nav.infotrygd.beregningsgrunnlag.values.FodselNr
import org.springframework.stereotype.Service
import java.time.LocalDate
import javax.transaction.Transactional

@Service
@Transactional
class ForeldrepengerService(
    private val periodeRepository: PeriodeRepository,
    private val vedtakBarnRepository: VedtakBarnRepository
) {
    fun hentForeldrepenger(fodselNr: FodselNr, fom: LocalDate, tom: LocalDate?): List<Foreldrepenger> {

        val stoenadstyper = listOf(
            Stoenadstype.ADOPSJON,
            Stoenadstype.FOEDSEL,
            Stoenadstype.RISIKOFYLT_ARBMILJOE,
            Stoenadstype.SVANGERSKAP
        )

        val result = if(tom != null) {
            periodeRepository.findByFnrAndStoenadstypeAndDates(fodselNr, stoenadstyper, fom, tom)
        } else {
            periodeRepository.findByFnrAndStoenadstypeAndDates(fodselNr, stoenadstyper, fom)
        }

        return result.map { periode ->
            val vedtak = vedtakBarnRepository.findByPersonKeyAndArbufoerSeqAndKode(periode.personKey,
                periode.arbufoerSeq.toString(), periode.barnKode)
            periodeToForeldrepenger(periode, vedtak)
        }
    }
}