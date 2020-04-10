package no.nav.infotrygd.beregningsgrunnlag.repository

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.infotrygd.beregningsgrunnlag.model.ip.Personkort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface PersonkortRepository : JpaRepository<Personkort, Long> {
    @Query("""
        SELECT p FROM Personkort p
         WHERE p.kontonummer = :kontonummer
           AND p.person.fnr = :fnr
        """)
    fun findByKontonummerAndFnr(kontonummer: Long, fnr: FoedselsNr): List<Personkort>
}