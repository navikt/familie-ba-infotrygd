package no.nav.infotrygd.barnetrygd.repository

import no.nav.infotrygd.barnetrygd.model.db2.Vedtak
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface VedtakRepository : JpaRepository<Vedtak, Long> {

    @Query("""
        SELECT v FROM Vedtak v
            INNER JOIN LøpeNrFnr l
            ON v.løpenummer = l.personLøpenummer
        WHERE l.personnummer = :fnr
        AND v.saksblokk = :saksblokk
        AND v.saksnummer = :saksnummer
    """)
    fun hentVedtak(fnr: String,
                   saksnummer: Long,
                   saksblokk: String): List<Vedtak>

    @Query(
        """
        SELECT count(*) FROM LøpeNrFnr l, StønadDb2 s, Vedtak v, Endring e
        WHERE l.personnummer = :fnr
        AND s.løpenummer = l.personLøpenummer
        AND s.kodeRutine = 'BA'
        AND v.løpenummer = s.løpenummer
        AND v.stønadId = s.stønadId
        AND v.kodeRutine = s.kodeRutine
        AND v.kodeResultat <> 'HB'
        AND e.vedtakId = v.vedtakId
        AND e.kode <> 'UA'
        AND e.kode <> 'AN'
        AND NOT EXISTS (SELECT 1 FROM Beslutning b
                        WHERE b.vedtakId = v.vedtakId
                        AND b.godkjent2 = 'J')
        AND EXISTS (SELECT 1 FROM Delytelse d
                    WHERE d.vedtakId = v.vedtakId)
    """
    )
    fun tellAntallÅpneSakerPåPerson(fnr: String): Long
}