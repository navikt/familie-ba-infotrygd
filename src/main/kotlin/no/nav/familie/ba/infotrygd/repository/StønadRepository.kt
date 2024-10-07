package no.nav.familie.ba.infotrygd.repository

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.familie.ba.infotrygd.model.dl1.Sak
import no.nav.familie.ba.infotrygd.model.dl1.Stønad
import no.nav.familie.ba.infotrygd.model.dl1.TrunkertStønad
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

    @Query("""SELECT new no.nav.familie.ba.infotrygd.model.dl1.TrunkertStønad(s.id, s.personKey, s.fnr, s.sakNr, s.saksblokk, s.status, s.region, s.virkningFom, s.opphørtFom, s.iverksattFom, s.antallBarn, p.pensjonstrygdet) FROM Stønad s
           INNER JOIN Person p
                   ON (s.personKey = p.personKey and
                       s.region = p.region)
        WHERE p.fnr = :fnr
        AND s.antallBarn > 0
        AND EXISTS (SELECT u FROM Utbetaling  u
            WHERE u.personKey = s.personKey   
            AND u.startUtbetalingMåned = s.iverksattFom
            AND u.virksomFom = s.virkningFom
            AND u.utbetalingstype = 'M'
            AND (u.utbetalingTom = '000000' or CAST(substring(u.utbetalingTom, 3, 4) as integer) >= :år))
    """)
    fun findTrunkertStønadMedUtbetalingÅrByFnr(fnr: FoedselsNr, år: Int): List<TrunkertStønad>

    @Query(
        """SELECT new no.nav.familie.ba.infotrygd.model.dl1.TrunkertStønad(s.id, s.personKey, s.fnr, s.sakNr, s.saksblokk, s.status, s.region, s.virkningFom, s.opphørtFom, s.iverksattFom, s.antallBarn, '')
        FROM Stønad s
        WHERE (s.opphørtFom='000000' or CAST(substring(s.opphørtFom, 3, 4) as integer) >= :år)
        AND CAST(substring(s.virkningFom, 1, 4) as integer) >= (9999 - :år)
        AND s.status in :statusKoder
        AND s.antallBarn > 0
        AND EXISTS (SELECT u FROM Utbetaling  u
                    WHERE u.personKey = s.personKey   
                    AND u.startUtbetalingMåned = s.iverksattFom
                    AND u.virksomFom = s.virkningFom
                    AND u.utbetalingstype = 'M'
                    AND (u.utbetalingTom = '000000' or CAST(substring(u.utbetalingTom, 3, 4) as integer) >= :år))"""
    )
    fun findStønadMedUtbetalingByÅrAndStatusKoder(år: Int, vararg statusKoder: String): List<TrunkertStønad>

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



    @Query("""
        SELECT s FROM Stønad s
        WHERE s.personKey = :personKey
        AND s.iverksattFom = :iverksattFom
        AND s.virkningFom = :virkningFom
        AND s.region = :region
    """)
    fun findStønad(personKey: Long, iverksattFom: String, virkningFom: String, region: String): Stønad

}
