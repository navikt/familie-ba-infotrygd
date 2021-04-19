package no.nav.infotrygd.barnetrygd.repository

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.infotrygd.barnetrygd.model.dl1.Barn
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface BarnRepository : JpaRepository<Barn, Long> {
    
    @Query("""
        SELECT b FROM Barn b
        WHERE b.barnFnr IN :barnFnrList
        AND b.barnetrygdTom = '000000'
    """)
    fun findBarnetrygdBarnInFnrList(barnFnrList: List<FoedselsNr>): List<Barn>

    @Query("""
        SELECT b FROM Barn b
        WHERE b.barnFnr IN :barnFnrList
    """)
    fun findBarnByFnrList(barnFnrList: List<FoedselsNr>): List<Barn>
}