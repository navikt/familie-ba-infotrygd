package no.nav.infotrygd.beregningsgrunnlag.repository

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.infotrygd.beregningsgrunnlag.model.Periode
import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Stoenadstype
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface PeriodeRepository : JpaRepository<Periode, Long> {

    @Query("""
        SELECT p FROM Periode p
         WHERE p.fnr = :fnr
           AND p.stoenadstype IN :stoenadstyper
           AND p.frisk != 'H'
           AND p.arbufoer != 0
    """)
    fun findByFnrAndStoenadstype(fnr: FoedselsNr, stoenadstyper: List<Stoenadstype>): List<Periode>

    @Query("""
        SELECT p FROM Periode p
         WHERE p.morFnr = :barnFnr
           AND p.stoenadstype IN ('OP', 'PB', 'PI', 'OM', 'PP')
           AND p.frisk NOT IN ('H', 'B', 'T', 'A', 'F', 'P')
           AND p.arbufoer != 0
    """)
    fun findByBarnFnr(barnFnr: FoedselsNr): List<Periode>
}
