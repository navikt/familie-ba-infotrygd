package no.nav.infotrygd.barnetrygd.repository

import no.nav.infotrygd.barnetrygd.model.Stønad
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface StønadRepository : JpaRepository<Stønad, Long> {

    @Query("""
        SELECT s FROM Stønad s
        WHERE s.personKey = :personKey
        AND s.region = :region
        AND s.opphørtFom = '000000'
    """)
    fun findByPersonKeyAndRegion(personKey: Long,
                                 region: String): List<Stønad>
}