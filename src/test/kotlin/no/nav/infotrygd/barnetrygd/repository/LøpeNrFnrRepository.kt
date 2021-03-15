package no.nav.infotrygd.barnetrygd.repository

import no.nav.infotrygd.barnetrygd.model.db2.LøpeNrFnr

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface LøpeNrFnrRepository : JpaRepository<LøpeNrFnr, Long>