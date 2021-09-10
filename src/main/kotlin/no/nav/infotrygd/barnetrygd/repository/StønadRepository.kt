package no.nav.infotrygd.barnetrygd.repository

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.infotrygd.barnetrygd.model.dl1.Stønad
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface StønadRepository : JpaRepository<Stønad, Long> {

    @Deprecated("Erstattes med direkte oppslag basert på fnr via inner join")
    @Query("""
        SELECT s FROM Stønad s
        WHERE s.personKey = :personKey
        AND s.region = :region
        AND s.opphørtFom = '000000'
    """)
    fun findByPersonKeyAndRegion(personKey: Long,
                                 region: String): List<Stønad>

    @Query("""
        SELECT s FROM Stønad s
           INNER JOIN Person p
                   ON (s.personKey = p.personKey and
                       s.region = p.region)
        WHERE p.fnr IN :fnr
    """)
    fun findStønadByFnr(fnr: List<FoedselsNr>): List<Stønad>

    @Query("SELECT new no.nav.infotrygd.barnetrygd.repository.TrunkertStønad(s.id, s.personKey, s.fnr, s.sakNr, s.saksblokk, s.status, s.region) FROM Stønad s " +
           "WHERE (s.opphørtFom='000000' or CAST(substring(s.opphørtFom, 3, 4) as integer) >= :år) " +
           "AND CAST(substring(s.virkningFom, 1, 4) as integer) >= (9999 - :år) " + //datoformatet er av typen "seq" derav 9999 - år
           "AND s.status in :statusKoder ")
    fun findStønadByÅrAndStatusKoder(år: Int, vararg statusKoder: String): List<TrunkertStønad>

    @Query("""
        SELECT s FROM Stønad s
           INNER JOIN Person p
                   ON (s.personKey = p.personKey and
                       s.region = p.region)
        WHERE p.fnr IN :fnr
        AND s.opphørtFom = '000000'
    """)
    fun findLøpendeStønadByFnr(fnr: List<FoedselsNr>): List<Stønad>

    @Query("""
        SELECT s FROM Stønad s
           INNER JOIN Barn b
                   ON (s.personKey = b.personKey and
                       s.region = b.region)
        WHERE b.barnFnr IN :barnFnr
    """)
    fun findStønadByBarnFnr(barnFnr: List<FoedselsNr>): List<Stønad>

    @Query("""
        SELECT s FROM Stønad s
           INNER JOIN Barn b
                   ON (s.personKey = b.personKey and
                       s.region = b.region)
        WHERE b.barnFnr IN :barnFnr
        AND b.barnetrygdTom = '000000'
        AND s.opphørtFom = '000000'
    """)
    fun findLøpendeStønadByBarnFnr(barnFnr: List<FoedselsNr>): List<Stønad>



    @Query("""
        SELECT s FROM Stønad s
           INNER JOIN Person p
                   ON (s.personKey = p.personKey and
                       s.region = p.region)
        AND s.opphørtFom = '000000'
    """)
    fun findLøpendeStønader(page: Pageable): List<Stønad>

}

data class TrunkertStønad(
    val id: Long,

    val personKey: Long,

    val fnr: FoedselsNr?,

    val sakNr: String,

    val saksblokk: String,

    val status: String,

    val region: String,
)