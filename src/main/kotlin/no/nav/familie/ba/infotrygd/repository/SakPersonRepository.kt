package no.nav.familie.ba.infotrygd.repository

import no.nav.familie.ba.infotrygd.model.dl1.SakPerson
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SakPersonRepository : JpaRepository<SakPerson, Long>