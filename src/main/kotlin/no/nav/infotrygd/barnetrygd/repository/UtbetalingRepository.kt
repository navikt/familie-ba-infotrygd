package no.nav.infotrygd.barnetrygd.repository

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.infotrygd.barnetrygd.model.db2.Utbetaling
import no.nav.infotrygd.barnetrygd.model.db2.Vedtak
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
}