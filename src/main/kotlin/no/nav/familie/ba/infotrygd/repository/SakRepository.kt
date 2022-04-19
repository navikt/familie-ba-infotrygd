package no.nav.familie.ba.infotrygd.repository

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.familie.ba.infotrygd.model.dl1.Sak
import no.nav.familie.ba.infotrygd.model.dl1.Stønad
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface SakRepository : JpaRepository<Sak, Long> {

    @Query(
        """
        SELECT s FROM Sak s
        INNER JOIN SakPerson p
                ON (s.personKey = p.personKey AND
                    s.region = p.region)
            WHERE p.fnr = :fnr 
              AND s.kapittelNr = 'BA' 
              AND s.type IN ('S', 'R', 'K', 'A', 'FL', 'AS')"""
    )  // søknad, revurdering, klage, anke, flyttesak, automatisk stønad
    fun findBarnetrygdsakerByFnr(fnr: FoedselsNr): List<Sak>

    @Query(
        """
        SELECT sak FROM Sak sak
           INNER JOIN Barn barn 
                   ON (sak.personKey = barn.personKey AND 
                       sak.region = barn.region)
           WHERE barn.barnFnr IN :barnFnr
             AND sak.kapittelNr = 'BA' 
             AND sak.type IN ('S', 'R', 'K', 'A', 'FL', 'AS')"""
    ) // søknad, revurdering, klage, anke, flyttesak, automatisk stønad
    fun findBarnetrygdsakerByBarnFnr(barnFnr: List<FoedselsNr>): List<Sak>


    @Query(
        """
        SELECT s FROM Sak s 
            WHERE s.personKey = :#{#stonad.personKey}
              AND s.kapittelNr = 'BA'
              AND s.valg = :valg
              AND s.undervalg = :undervalg
              AND s.saksblokk = :#{#stonad.saksblokk}
              AND s.saksnummer = :#{#stonad.sakNr}
              AND s.region = :#{#stonad.region}
              AND s.type IN ('S', 'R', 'K', 'A', 'FL', 'AS')"""
    )  // søknad, revurdering, klage, anke, flyttesak, automatisk stønad
    fun findBarnetrygdsakerByStønad(stonad: Stønad, valg: String, undervalg: String): List<Sak>


    @Query(
        """
        SELECT s FROM Sak s 
            WHERE s.personKey = :#{#stonad.personKey}
              AND s.kapittelNr = 'BA'
              AND s.valg = 'UT'
              AND s.saksblokk = :#{#stonad.saksblokk}
              AND s.saksnummer = :#{#stonad.sakNr}
              AND s.region = :#{#stonad.region}
              AND s.type IN ('S', 'R', 'K', 'A', 'FL', 'AS')"""
    )
    fun hentUtvidetBarnetrygdsakerForStønad(stonad: Stønad): List<Sak>



}