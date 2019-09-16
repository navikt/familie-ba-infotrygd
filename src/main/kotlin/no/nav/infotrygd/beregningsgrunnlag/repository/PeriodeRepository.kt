package no.nav.infotrygd.beregningsgrunnlag.repository

import no.nav.infotrygd.beregningsgrunnlag.model.Periode
import no.nav.infotrygd.beregningsgrunnlag.values.FodselNr
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface PeriodeRepository : JpaRepository<Periode, Long> {

    @Query("""
        SELECT p FROM Periode p
         WHERE p.fnr = :fnr
           AND p.behandlingstema = 'SV'
           AND p.frisk = 'F'
           AND p.arbufoer >= :fom
    """)
    fun findAvsluttedeSakerByFnr(fnr: FodselNr, fom: LocalDate): List<Periode>

    @Query("""
        SELECT COUNT(p) FROM Periode p
         WHERE p.behandlingstema = 'SV'
           AND p.frisk = 'F'
           AND p.arbufoer >= :fom
    """)
    fun countAvsluttedeSaker(fom: LocalDate): Long

    @Query("""
        SELECT p FROM Periode p
         WHERE p.fnr = :fnr
           AND p.behandlingstema = 'SV'
           AND p.frisk = ' '
    """)
    fun findOpneSakerMedLopendeUtbetalingByFnr(fnr: FodselNr): List<Periode>

    @Query("""
        SELECT COUNT(p) FROM Periode p
         WHERE p.behandlingstema = 'SV'
           AND p.frisk = ' '
    """)
    fun countOpneSakerMedLopendeUtbetaling(): Long

    @Query("""
        SELECT p FROM Periode p
         WHERE p.behandlingstema = 'SV'
           AND p.frisk = ' '
    """)
    fun findOpneSakerMedLopendeUtbetaling(): List<Periode>
}