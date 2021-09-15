package no.nav.infotrygd.barnetrygd.repository

import no.nav.infotrygd.barnetrygd.model.dl1.SakPerson
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SakPersonRepository : JpaRepository<SakPerson, Long>