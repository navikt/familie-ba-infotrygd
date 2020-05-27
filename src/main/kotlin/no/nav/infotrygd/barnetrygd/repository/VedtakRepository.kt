package no.nav.infotrygd.barnetrygd.repository

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.infotrygd.barnetrygd.model.db2.Vedtak
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface VedtakRepository : JpaRepository<Vedtak, Long> {

    @Query("""
        SELECT v FROM Vedtak v
          JOIN v.alleDelytelser D
         WHERE v.person.fnr = :fnr
           AND v.stonad.kodeRutine IN ('BS', 'BR')
           AND exists (
                FROM D d 
                 WHERE d.vedtakId = v.id
                   AND d.type = 'PN')
    """)
    fun findByFnr(fnr: FoedselsNr): List<Vedtak>

    @Query("""
        SELECT v FROM Vedtak v
          JOIN v.alleDelytelser D
         WHERE v.stonad.stonadBs.barn.fnr = :barnFnr
           AND v.stonad.kodeRutine IN ('BS', 'BR')
           AND exists (
                FROM D d
                 WHERE d.vedtakId = v.id
                   AND d.type = 'PN')
    """)
    fun findByBarnFnr(barnFnr: FoedselsNr): List<Vedtak>
}