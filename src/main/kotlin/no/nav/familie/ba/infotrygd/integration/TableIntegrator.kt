package no.nav.familie.ba.infotrygd.integration

import org.hibernate.boot.Metadata
import org.hibernate.boot.spi.BootstrapContext
import org.hibernate.engine.spi.SessionFactoryImplementor
import org.hibernate.integrator.spi.Integrator
import org.hibernate.jpa.boot.spi.IntegratorProvider
import org.hibernate.service.spi.SessionFactoryServiceRegistry
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer
import org.springframework.stereotype.Component


@Component
class TableIntegrator : Integrator {
    private lateinit var _tables: Map<String, List<String>>

    val tables: Map<String, List<String>>
        get() {
            return _tables
        }

    override fun integrate(
        metadata: Metadata,
        bootstrapContext: BootstrapContext,
        sessionFactory: SessionFactoryImplementor
    ) {
        val result = mutableMapOf<String, List<String>>()

        for (namespace in metadata
            .database
            .namespaces) {

            for (table in namespace.tables) {
                val cols = table.columns.toList()
                val names = cols.map { it.canonicalName }
                result[table.name] = names
            }
        }
        _tables = result
    }

    override fun disintegrate(
        sessionFactory: SessionFactoryImplementor,
        serviceRegistry: SessionFactoryServiceRegistry
    ) {

    }
}

@Component
class TableIntegratorProvider(private val tableIntegrator: TableIntegrator) : IntegratorProvider {
    override fun getIntegrators(): MutableList<Integrator> {
        return mutableListOf(tableIntegrator)
    }
}

@Component
class HibernateConfig(private val tableIntegratorProvider: TableIntegratorProvider) : HibernatePropertiesCustomizer {

    override fun customize(hibernateProperties: MutableMap<String, Any>) {
        hibernateProperties["hibernate.integrator_provider"] = tableIntegratorProvider
    }
}