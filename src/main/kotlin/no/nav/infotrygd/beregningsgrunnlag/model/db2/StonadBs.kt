package no.nav.infotrygd.beregningsgrunnlag.model.db2

import no.nav.infotrygd.beregningsgrunnlag.model.converters.BrukerIdConverter
import javax.persistence.*

@Entity
@Table(name = "T_STONAD_BS")
data class StonadBs(
    @Id
    @Column(name = "STONAD_ID", columnDefinition = "DECIMAL")
    val id: Long,

    @Column(name = "BRUKERID", columnDefinition = "CHAR")
    @Convert(converter = BrukerIdConverter::class)
    val brukerId: String
)