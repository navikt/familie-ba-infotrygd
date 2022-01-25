package no.nav.familie.ba.infotrygd.repository

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.familie.ba.infotrygd.model.dl1.Hendelse
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface HendelseRepository : JpaRepository<Hendelse, Long> {

    @Query("""
        SELECT h FROM Hendelse h
        WHERE h.fnr in :fnr
        AND h.tekstKode1 in :tekstKoder
        AND h.aksjonsdatoSeq <= :aksjonsdatoSeq
    """)
    //f.eks alle brev etter 2021-12-01 gir (99999999 - 20211201) gir aksjonsdatoSeq 79788798
    fun findHendelseByFnrInAndTekstKoderIn(fnr: List<FoedselsNr>, tekstKoder: List<String>, aksjonsdatoSeq: Long): List<Hendelse>

}