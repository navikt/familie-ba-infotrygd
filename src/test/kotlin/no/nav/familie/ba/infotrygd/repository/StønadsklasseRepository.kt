package no.nav.familie.ba.infotrygd.repository

import no.nav.familie.ba.infotrygd.model.db2.Stønadsklasse
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface StønadsklasseRepository : JpaRepository<Stønadsklasse, Long>