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

    @Query("""
        WITH personermedutvidetbarnetrygd AS (
            SELECT stonad.F_NR FROM {h-schema}BA_STOENAD_20 stonad
                INNER JOIN {h-schema}SA_SAK_10 sak ON (
                    sak.S01_PERSONKEY = stonad.B01_PERSONKEY AND
                    sak.S05_SAKSBLOKK = stonad.B20_BLOKK AND
                    sak.S10_SAKSNR = stonad.B20_SAK_NR AND
                    sak.REGION = stonad.REGION)
            WHERE (
                stonad.B20_OPPHOERT_VFOM  = '000000' OR
                CAST(substring(stonad.B20_OPPHOERT_VFOM, 3, 4) AS integer) >= :aar
            )
            AND CAST(substring(stonad.B20_VIRKFOM_SEQ, 1, 4) AS integer) >= :aarSeq
            AND sak.S10_KAPITTELNR = 'BA'
            AND (stonad.B20_STATUS IN ('02', '03') OR
                (stonad.B20_STATUS = '00' AND sak.S10_VALG = 'UT' AND sak.S10_UNDERVALG IN ('MD', 'ME', 'MB')))
        )
        
        SELECT s.F_NR AS ident, MIN(s.B20_IVERFOM_SEQ) AS sisteVedtaksdatoSeq FROM {h-schema}BA_STOENAD_20 s
            INNER JOIN {h-schema}BA_PERSON_01 p ON s.B01_PERSONKEY = p.B01_PERSONKEY AND s.REGION = p.REGION
        WHERE p.F_NR IN (SELECT * FROM personermedutvidetbarnetrygd)
        GROUP BY s.F_NR
        """,
           nativeQuery = true
    )
    fun findPersonerMedUtvidetBarnetrygd(aar: Int, aarSeq: Int): List<PersonIdentOgSisteVedtaksdato>

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

interface PersonIdentOgSisteVedtaksdato {
    val ident: String
    val sisteVedtaksdatoSeq: String
}

