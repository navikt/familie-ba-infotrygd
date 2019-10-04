package no.nav.infotrygd.beregningsgrunnlag.repository

import no.nav.infotrygd.beregningsgrunnlag.model.db2.Vedtak
import no.nav.infotrygd.beregningsgrunnlag.values.FoedselNr
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface VedtakRepository : JpaRepository<Vedtak, Long> {

    @Query("""
        SELECT v FROM Vedtak v
         WHERE v.person.fnr = :fnr
    """)
    fun findByFnr(fnr: FoedselNr): List<Vedtak>

    @Query("""
        SELECT v FROM Vedtak v
         WHERE v.person.fnr = :fnr
           AND v.datoStart >= :fom
           AND v.stonad.kodeRutine in ('BS', 'BR')
    """)
    fun findByFnrAndStartDato(fnr: FoedselNr, fom: LocalDate): List<Vedtak>

    @Query("""
        SELECT v FROM Vedtak v
         WHERE v.person.fnr = :fnr
           AND v.datoStart >= :fom
           AND v.datoStart <= :tom
           AND v.stonad.kodeRutine in ('BS', 'BR')
    """)
    fun findByFnrAndStartDato(fnr: FoedselNr, fom: LocalDate, tom: LocalDate): List<Vedtak>
}