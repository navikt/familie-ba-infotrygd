package no.nav.infotrygd.beregningsgrunnlag.repository

import no.nav.infotrygd.beregningsgrunnlag.model.Periode
import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Stoenadstype
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
           AND p.stoenadstype IN :stoenadstyper
           AND p.arbufoer >= :fom
           AND p.arbufoer <= :tom
    """)
    fun findByFnrAndStoenadstypeAndDates(fnr: FodselNr, stoenadstyper: List<Stoenadstype>, fom: LocalDate, tom: LocalDate): List<Periode>

    @Query("""
        SELECT p FROM Periode p
         WHERE p.fnr = :fnr
           AND p.stoenadstype IN :stoenadstyper
           AND p.arbufoer >= :fom
    """)
    fun findByFnrAndStoenadstypeAndDates(fnr: FodselNr, stoenadstyper: List<Stoenadstype>, fom: LocalDate): List<Periode>
}
