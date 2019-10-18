package no.nav.infotrygd.beregningsgrunnlag.service

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.infotrygd.beregningsgrunnlag.dto.PaaroerendeSykdom
import no.nav.infotrygd.beregningsgrunnlag.dto.vedtakToPaaroerendeSykdom
import no.nav.infotrygd.beregningsgrunnlag.repository.VedtakRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import javax.transaction.Transactional

@Service
@Transactional
class BarnSykdomService(private val vedtakRepository: VedtakRepository) {
    fun barnsSykdom(fnr: FoedselsNr, fom: LocalDate, tom: LocalDate?): List<PaaroerendeSykdom> {
        val vedtak = if(tom == null) {
            vedtakRepository.findByFnrAndStartDato(fnr, fom)
        } else {
            vedtakRepository.findByFnrAndStartDato(fnr, fom, tom)
        }

        return vedtak.map { vedtakToPaaroerendeSykdom(it) }
    }
}