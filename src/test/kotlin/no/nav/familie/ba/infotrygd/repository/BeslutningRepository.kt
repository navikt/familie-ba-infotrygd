package no.nav.familie.ba.infotrygd.repository

import no.nav.familie.ba.infotrygd.model.db2.Beslutning
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BeslutningRepository : JpaRepository<Beslutning, Long>