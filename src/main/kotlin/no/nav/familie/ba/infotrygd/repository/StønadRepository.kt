package no.nav.familie.ba.infotrygd.repository

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.familie.ba.infotrygd.model.dl1.Sak
import no.nav.familie.ba.infotrygd.model.dl1.Stønad
import org.springframework.data.domain.Page
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

    @Query("SELECT new no.nav.familie.ba.infotrygd.repository.TrunkertStønad(s.id, s.personKey, s.fnr, s.sakNr, s.saksblokk, s.status, s.region, s.virkningFom, s.opphørtFom, s.iverksattFom) FROM Stønad s " +
           "WHERE (s.opphørtFom='000000' or CAST(substring(s.opphørtFom, 3, 4) as integer) >= :år) " +
           "AND CAST(substring(s.virkningFom, 1, 4) as integer) >= (9999 - :år) " + //datoformatet er av typen "seq" derav 9999 - år
           "AND s.status in :statusKoder " +
           "AND s.antallBarn > 0")
    fun findStønadByÅrAndStatusKoder(år: Int, vararg statusKoder: String): List<TrunkertStønad>

    @Query("""SELECT s FROM Stønad s
                INNER JOIN Person p
                ON (s.personKey = p.personKey and
                        s.region = p.region)
                WHERE p.fnr = :fnr
                AND (s.opphørtFom='000000' or CAST(substring(s.opphørtFom, 3, 4) as integer) >= :år)
                AND CAST(substring(s.virkningFom, 1, 4) as integer) >= (9999 - :år)
                AND s.status in :statusKoder
                AND s.antallBarn > 0""")
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
        SELECT s FROM Stønad s           
           INNER JOIN Sak sa
                   ON ( s.personKey = sa.personKey and
                        s.region = sa.region and
                        s.saksblokk = sa.saksblokk and
                        s.sakNr = sa.saksnummer )
        WHERE s.opphørtFom = '000000'
        AND sa.kapittelNr = 'BA'
        AND sa.valg = :valg
        AND sa.undervalg = :undervalg
    """)
    fun findKlarForMigrering(page: Pageable, valg: String, undervalg: String): Page<Stønad>

    @Query(
        """
        SELECT s FROM Stønad s           
           INNER JOIN Sak sa
                   ON ( s.personKey = sa.personKey and
                        s.region = sa.region and
                        s.saksblokk = sa.saksblokk and
                        s.sakNr = sa.saksnummer )
        WHERE s.opphørtFom = '000000'
        AND sa.kapittelNr = 'BA'
        AND sa.valg = :valg
        AND sa.undervalg = :undervalg
        AND s.tkNr IN ('0312','0315')
    """)
    fun findKlarForMigreringIPreprod(page: Pageable, valg: String, undervalg: String): Page<Stønad>

    @Query(
        value = """
        SELECT sa.S10_VALG valg, sa.S10_UNDERVALG undervalg, count(*) antall FROM {h-schema}BA_STOENAD_20 s
            INNER JOIN {h-schema}SA_SAK_10 sa
                ON ( s.B01_PERSONKEY = sa.S01_PERSONKEY and
                     s.region = sa.region and
                     s.B20_BLOKK = sa.S05_SAKSBLOKK and
                     s.B20_SAK_NR = sa.S10_SAKSNR )
        WHERE s.B20_OPPHOERT_VFOM = '000000'
        AND sa.S10_KAPITTELNR = 'BA'
        group by sa.S10_VALG, sa.S10_UNDERVALG""",
        nativeQuery = true
    )
    fun countLøpendeStønader() : List<AntallLøpendeStønader>

    @Query("""
        SELECT s FROM Stønad s
        WHERE s.personKey = :personKey
        AND s.iverksattFom = :iverksattFom
        AND s.virkningFom = :virkningFom
        AND s.region = :region
    """)
    fun findStønad(personKey: Long, iverksattFom: String, virkningFom: String, region: String): Stønad

}
interface AntallLøpendeStønader {
    val valg: String
    val undervalg: String
    val antall: Int
}

data class TrunkertStønad(
    val id: Long,

    val personKey: Long,

    val fnr: FoedselsNr?,

    val sakNr: String,

    val saksblokk: String,

    val status: String,

    val region: String,

    val virkningFom: String,

    val opphørtFom: String,

    val iverksattFom: String
)
