package no.nav.familie.ba.infotrygd.repository

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.familie.ba.infotrygd.model.dl1.Barn
import no.nav.familie.ba.infotrygd.model.dl1.TrunkertStønad
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface BarnRepository : JpaRepository<Barn, Long> {

    @Query("""
        SELECT b FROM Barn b
        WHERE b.barnFnr IN :barnFnrList
    """)
    fun findBarnByFnrList(barnFnrList: List<FoedselsNr>): List<Barn>

    @Query("""
        SELECT b FROM Barn b
        WHERE b.personKey = :#{#stonad.personKey}
        AND b.iverksatt = :#{#stonad.iverksattFom}
        AND b.virkningFom = :#{#stonad.virkningFom}
        AND b.region = :#{#stonad.region}
    """)
    fun findBarnByStønad(stonad: TrunkertStønad): List<Barn>


    @Query("""
        SELECT b FROM Barn b
        WHERE b.personKey = :personKey
        AND (b.barnetrygdTom = '000000' OR :historikk = true)
    """)
    fun findBarnByPersonkey(personKey: Long, historikk: Boolean = false): List<Barn>



}