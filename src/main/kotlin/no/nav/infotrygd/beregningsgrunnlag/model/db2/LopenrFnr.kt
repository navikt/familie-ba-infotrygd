package no.nav.infotrygd.beregningsgrunnlag.model.db2

import no.nav.infotrygd.beregningsgrunnlag.model.converters.FoedselNrConverter
import no.nav.infotrygd.beregningsgrunnlag.values.FoedselNr
import javax.persistence.*

@Entity
@Table(name = "T_LOPENR_FNR")
data class LopenrFnr(
    @Id
    @Column(name = "PERSON_LOPENR", nullable = false, columnDefinition = "DECIMAL")
    val id: Long,

    @Column(name = "PERSONNR", nullable = false, columnDefinition = "CHAR")
    @Convert(converter = FoedselNrConverter::class)
    val fnr: FoedselNr
)