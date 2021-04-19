package no.nav.infotrygd.barnetrygd.repository

import no.nav.infotrygd.barnetrygd.model.db2.StønadDb2
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface StønadDb2Repository : JpaRepository<StønadDb2, Long>