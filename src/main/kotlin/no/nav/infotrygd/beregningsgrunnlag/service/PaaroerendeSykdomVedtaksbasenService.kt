package no.nav.infotrygd.beregningsgrunnlag.service

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.infotrygd.beregningsgrunnlag.model.db2.Vedtak
import no.nav.infotrygd.beregningsgrunnlag.repository.VedtakRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import javax.transaction.Transactional

@Service
@Transactional
class PaaroerendeSykdomVedtaksbasenService(private val vedtakRepository: VedtakRepository) {
    fun barnsSykdom(fnr: FoedselsNr, fom: LocalDate, tom: LocalDate?): List<Vedtak> {
        return if(tom == null) {
            vedtakRepository.findByFnrAndStartDato(fnr, fom)
        } else {
            vedtakRepository.findByFnrAndStartDato(fnr, fom, tom)
        }
    }
}