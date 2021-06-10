package no.nav.infotrygd.barnetrygd.repository

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.infotrygd.barnetrygd.model.dl1.Sak
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface SakRepository : JpaRepository<Sak, Long> {

    @Query(
        """
        SELECT s FROM Sak s 
            WHERE s.person.fnr = :fnr 
              AND s.kapittelNr = 'BA' 
              AND s.type IN ('S', 'R', 'K', 'A', 'FL')"""
    )  // søknad, revurdering, klage, anke, flyttesak
    fun findBarnetrygdsakerByFnr(fnr: FoedselsNr): List<Sak>

    @Query(
        """
        SELECT sak FROM Sak sak
           INNER JOIN Barn barn 
                   ON (sak.personKey = barn.personKey and 
                       sak.region = barn.region)
           WHERE barn.barnFnr IN :barnFnr
             AND sak.kapittelNr = 'BA' 
             AND sak.type IN ('S', 'R', 'K', 'A', 'FL')"""
    ) // søknad, revurdering, klage, anke, flyttesak
    fun findBarnetrygdsakerByBarnFnr(barnFnr: List<FoedselsNr>): List<Sak>


    @Query(
        """
        SELECT s FROM Sak s 
            WHERE s.person.personKey = :personKey
              AND s.kapittelNr = 'BA'
              AND s.valg = :valg
              AND s.undervalg = :undervalg
              AND s.saksblokk = :saksblokk
              AND s.saksnummer = :saksnummer
              AND s.region = :region
              AND s.type IN ('S', 'R', 'K', 'A', 'FL')"""
    )  // søknad, revurdering, klage, anke, flyttesak
    fun findBarnetrygdsakerByStønad(personKey: Long, valg: String, undervalg: String, saksblokk: String, saksnummer:String, region: String): List<Sak>
}