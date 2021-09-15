package no.nav.infotrygd.barnetrygd.repository

import no.nav.infotrygd.barnetrygd.model.dl1.Sak
import no.nav.infotrygd.barnetrygd.model.dl1.Status
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface StatusRepository : JpaRepository<Status, Long> {

    @Query("""
        SELECT s From Status s
        WHERE s.personKey = :#{#sak.personKey}
        AND s.saksblokk = :#{#sak.saksblokk}
        AND s.saksnummer = :#{#sak.saksnummer}
        AND s.region = :#{#sak.region}
    """)
    fun findStatushistorikkForSak(sak: Sak): List<Status>
}
