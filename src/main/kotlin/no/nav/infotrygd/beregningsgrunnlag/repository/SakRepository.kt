package no.nav.infotrygd.beregningsgrunnlag.repository

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.infotrygd.beregningsgrunnlag.model.sak.Sak
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface SakRepository : JpaRepository<Sak, Long> {
    @Query("""
        SELECT s FROM Sak s
         WHERE s.fnr = :fnr
           AND s.type in ('S', 'R', 'K', 'A')
           AND s.kapittelNr = 'BS'
           AND s.valg IN ('OP', 'PB', 'OM', 'PN', 'PI', 'PP')
    """)
    fun findPaaroerendeSykdomByFnr(fnr: FoedselsNr): List<Sak>
}