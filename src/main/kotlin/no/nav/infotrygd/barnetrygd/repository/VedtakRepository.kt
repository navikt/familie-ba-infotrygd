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
}