package no.nav.familie.ba.infotrygd.repository

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.familie.ba.infotrygd.model.db2.Utbetaling
import no.nav.familie.ba.infotrygd.model.dl1.TrunkertStønad
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface UtbetalingRepository : JpaRepository<Utbetaling, Long> {

    @Query("""
        SELECT utbet FROM Utbetaling utbet
        WHERE utbet.personKey = :#{#stonad.personKey}   
        AND utbet.startUtbetalingMåned = :#{#stonad.iverksattFom}
        AND utbet.virksomFom = :#{#stonad.virkningFom}
        AND utbet.utbetalingstype = 'M'
    """)
    fun hentUtbetalingerByStønad(stonad: TrunkertStønad): List<Utbetaling>
}