package no.nav.infotrygd.barnetrygd.repository

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.infotrygd.barnetrygd.model.db2.Utbetaling
import no.nav.infotrygd.barnetrygd.model.db2.Vedtak
import no.nav.infotrygd.barnetrygd.model.dl1.Stønad
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface UtbetalingRepository : JpaRepository<Utbetaling, Long> {

    @Query("""
        SELECT u FROM Utbetaling u
        WHERE u.fnr = :fnr
    """)
    fun hentUtbetalinger(fnr: FoedselsNr): List<Utbetaling>

    @Query("""
        SELECT u FROM Utbetaling u
        WHERE u.fnr = :fnr
        AND u.utbetalingstype = :type
    """)
    fun hentUtbetalingerByFnrType(fnr: FoedselsNr, type: String): List<Utbetaling>

    @Query("""
        SELECT utbet FROM Utbetaling utbet
        WHERE utbet.personKey = :#{#stonad.personKey}   
        AND utbet.startUtbetalingMåned = :#{#stonad.iverksattFom}
        AND utbet.virksomFom = :#{#stonad.virkningFom}
        AND utbet.utbetalingstype = 'M'
    """)
    fun hentUtbetalingerByStønad(stonad: Stønad): List<Utbetaling>
}