package no.nav.familie.ba.infotrygd.repository

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.familie.ba.infotrygd.model.dl1.Sak
import no.nav.familie.ba.infotrygd.model.dl1.Stønad
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface StønadRepository : JpaRepository<Stønad, Long> {

    @Query("""
        SELECT s FROM Stønad s
           INNER JOIN Person p
                   ON (s.personKey = p.personKey and
                       s.region = p.region)
        WHERE p.fnr IN :fnr
    """)
    fun findStønadByFnr(fnr: List<FoedselsNr>): List<Stønad>

    @Query("SELECT new no.nav.familie.ba.infotrygd.repository.TrunkertStønad(s.id, s.personKey, s.fnr, s.sakNr, s.saksblokk, s.status, s.region) FROM Stønad s " +
           "WHERE (s.opphørtFom='000000' or CAST(substring(s.opphørtFom, 3, 4) as integer) >= :år) " +
           "AND CAST(substring(s.virkningFom, 1, 4) as integer) >= (9999 - :år) " + //datoformatet er av typen "seq" derav 9999 - år
           "AND s.status in :statusKoder")
    fun findStønadByÅrAndStatusKoder(år: Int, vararg statusKoder: String): List<TrunkertStønad>

    @Query("""SELECT s FROM Stønad s
                INNER JOIN Person p
                ON (s.personKey = p.personKey and
                        s.region = p.region)
                WHERE p.fnr = :fnr
                AND (s.opphørtFom='000000' or CAST(substring(s.opphørtFom, 3, 4) as integer) >= :år)
                AND CAST(substring(s.virkningFom, 1, 4) as integer) >= (9999 - :år)
                AND s.status in :statusKoder""")
    fun findStønadByÅrAndStatusKoderAndFnr(fnr: FoedselsNr, år: Int, vararg statusKoder: String): List<Stønad>

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

    @Query("""
        SELECT s FROM Stønad s
        WHERE s.personKey = :#{#sak.personKey}
        AND s.saksblokk = :#{#sak.saksblokk}
        AND s.sakNr = :#{#sak.saksnummer}
        AND s.region = :#{#sak.region}
    """)
    fun findStønadBySak(sak: Sak): List<Stønad>

    @Query("""
        SELECT MIN(s.iverksattFom) FROM Stønad s
        WHERE s.personKey = :personKey
    """)
    fun findSenesteIverksattFomByPersonKey(personKey: Long): String

    @Query(
        """
        SELECT s.* FROM {h-schema}BA_STOENAD_20 s           
           INNER JOIN {h-schema}SA_SAK_10 sa
                   ON ( s.B01_PERSONKEY = sa.S01_PERSONKEY and
                        s.REGION = sa.REGION and
                        s.B20_BLOKK = sa.S05_SAKSBLOKK and
                        s.B20_SAK_NR = sa.S10_SAKSNR )
        WHERE s.B20_OPPHOERT_VFOM = '000000'
        AND sa.S10_KAPITTELNR = 'BA'
        AND sa.S10_VALG = :valg
        AND sa.S10_UNDERVALG = :undervalg
        AND s.B20_ANT_BARN <= :maksAntallBarn
        AND regexp_like(s.TK_NR, :tknrFilter)
    """,
        nativeQuery = true
    )
    fun findKlarForMigrering(page: Pageable, valg: String, undervalg: String, maksAntallBarn: Int = 99, tknrFilter: String = "...."): List<Stønad>

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
