package no.nav.infotrygd.beregningsgrunnlag.service

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.infotrygd.beregningsgrunnlag.dto.Foreldrepenger
import no.nav.infotrygd.beregningsgrunnlag.dto.periodeToForeldrepengerDetaljer
import no.nav.infotrygd.beregningsgrunnlag.dto.periodeToGrunnlag
import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Stoenadstype
import no.nav.infotrygd.beregningsgrunnlag.repository.PeriodeRepository
import no.nav.infotrygd.beregningsgrunnlag.repository.VedtakBarnRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import javax.transaction.Transactional

@Service
@Transactional
class ForeldrepengerService(
    private val periodeRepository: PeriodeRepository,
    private val vedtakBarnRepository: VedtakBarnRepository
) {
    fun hentForeldrepenger(stoenadstyper: List<Stoenadstype>, foedselsNr: FoedselsNr, fom: LocalDate, tom: LocalDate?): List<Foreldrepenger> {

        val result = if(tom != null) {
            periodeRepository.findByFnrAndStoenadstypeAndDates(foedselsNr, stoenadstyper, fom, tom)
        } else {
            periodeRepository.findByFnrAndStoenadstypeAndDates(foedselsNr, stoenadstyper, fom)
        }

        return result.map { periode ->
            val vedtak = periode.barnPersonKey?.let { barnPersonKey ->
                vedtakBarnRepository.findByPersonKeyAndArbufoerSeqAndKode(
                    personKey = barnPersonKey,
                    arbufoerSeq = periode.arbufoerSeq.toString(),
                    kode = periode.barnKode
                )
            }
            Foreldrepenger(
                generelt = periodeToGrunnlag(periode),
                foreldrepengerDetaljer = periodeToForeldrepengerDetaljer(periode, vedtak)
            )
        }
    }
}