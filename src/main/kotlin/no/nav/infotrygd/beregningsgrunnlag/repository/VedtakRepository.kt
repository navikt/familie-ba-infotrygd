package no.nav.infotrygd.beregningsgrunnlag.repository

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.infotrygd.beregningsgrunnlag.model.db2.Vedtak
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface VedtakRepository : JpaRepository<Vedtak, Long> {

    @Query("""
        SELECT v FROM Vedtak v
         WHERE v.person.fnr = :fnr
    """)
    fun findByFnr(fnr: FoedselsNr): List<Vedtak>

    @Query("""
        SELECT v FROM Vedtak v
          JOIN v.delytelser D
         WHERE v.person.fnr = :fnr
           AND v.stonad.kodeRutine IN ('BS', 'BR')
           AND exists (
                FROM D d 
                 WHERE d.vedtakId = v.id
                   AND d.type = 'PN')
    """)
    fun findByFnrAndStartDato(fnr: FoedselsNr): List<Vedtak>
}