package no.nav.infotrygd.barnetrygd.service

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.infotrygd.barnetrygd.model.db2.Vedtak
import no.nav.infotrygd.barnetrygd.repository.VedtakRepository
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
@Transactional
class PaaroerendeSykdomVedtaksbasenService(private val vedtakRepository: VedtakRepository) {
    fun barnsSykdom(fnr: FoedselsNr): List<Vedtak> {
        return vedtakRepository.findByFnr(fnr)
    }
}