package no.nav.infotrygd.barnetrygd.repository

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.infotrygd.barnetrygd.model.Sak
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface SakRepository : JpaRepository<Sak, Long> {

    @Query(
        """
        SELECT s FROM Sak s, Person p
        WHERE s.s01Personkey = p.personKey
        AND s.region = p.region
        AND p.fnr = :fnr
    """
    )
    fun findSakerPåPersonByFnr(fnr: FoedselsNr): List<Sak>
}