package no.nav.infotrygd.barnetrygd.repository

import no.nav.infotrygd.barnetrygd.model.db2.Endring
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface EndringRepository : JpaRepository<Endring, Long>