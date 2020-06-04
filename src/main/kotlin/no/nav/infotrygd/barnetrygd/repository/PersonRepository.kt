package no.nav.infotrygd.barnetrygd.repository

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.infotrygd.barnetrygd.model.Person
import no.nav.infotrygd.barnetrygd.model.Stønad
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface PersonRepository : JpaRepository<Person, Long> {

    @Query("""
        SELECT p FROM Person p
         WHERE p.fnr = :fnr
    """)
    fun findByFnr(fnr: FoedselsNr): Person?

    @Query("""
        SELECT p FROM Person p
         WHERE p.fnr IN :fnrList
    """)
    fun findByFnrList(fnrList: List<FoedselsNr>): List<Person>

    @Query("""
        SELECT DISTINCT p.stønader FROM Person p
                   JOIN p.stønader s
                  WHERE p.fnr = :fnr
                    AND s.opphørtFom = '000000'
    """)
    fun findStønadByFnr(fnr: FoedselsNr): List<Stønad>

    @Query("""
        SELECT DISTINCT p.stønader FROM Person p
                   JOIN p.stønader s
                   JOIN p.barn b
                  WHERE b.barnFnr = :barnFnr
                    AND b.barnetrygdTom = '000000'
                    AND s.opphørtFom = '000000'
    """)
    fun findStønadByBarnFnr(barnFnr: Long): List<Stønad>

}