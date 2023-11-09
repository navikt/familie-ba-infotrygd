package no.nav.familie.ba.infotrygd.repository

import no.nav.familie.ba.infotrygd.model.dl1.Person
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface PersonRepository : JpaRepository<Person, Long> {

    @Query("""
        SELECT p.mottakerNummer FROM Person p
        WHERE p.personKey = :personKey
    """)
    fun findMottakerNummerByPersonkey(personKey: Long): Long?

    @Query(
        """
        SELECT p.fnr as fnr,
               p.tkNr as tknr,
        CASE
            WHEN p.pensjonstrygdet = 'J' THEN 'Ja'
            WHEN p.pensjonstrygdet = 'N' THEN 'Nei'
            ELSE 'Ukjent'
        END as pensjonstrygdet
        FROM Person p
        WHERE substring(p.fnr, 5, 1) NOT IN ('8','9')
    """
    )
    fun findAllePensjonstrygdet(): List<String>
}