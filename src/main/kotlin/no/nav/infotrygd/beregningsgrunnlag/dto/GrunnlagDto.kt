package no.nav.infotrygd.beregningsgrunnlag.dto

import java.math.BigDecimal
import java.time.LocalDate

interface Grunnlag {
    val behandlingstema: Kodeverdi
    val identdato: LocalDate
    val periode: Periode?
    val arbeidskategori: Kodeverdi?
    val arbeidsforhold: List<Arbeidsforhold>
    val vedtak: List<Vedtak>
}

data class Vedtak(
    val utbetalingsgrad: Int?,
    val periode: Periode
)

data class Periode(
    val fom: LocalDate,
    val tom: LocalDate
)

data class Arbeidsforhold(
    val inntektForPerioden: BigDecimal?,
    val inntektsperiode: Kodeverdi,
    val arbeidsgiverOrgnr: String
)

data class Kodeverdi(val kode: String, val termnavn: String)

/*data class Foreldrepenger(
    val opprinneligIdentdato: LocalDate,
    val dekningsgrad: Int,
    val gradering: Int,
    val foedselsdatoBarn: LocalDate
) : Grunnlagsdetaljer

data class Sykepenger(
    val inntektsgrunnlagProsent: Int
) : Grunnlagsdetaljer


data class PaarorendeSykdom(
    val foedselsdatoPleietrengende: LocalDate
) : Grunnlagsdetaljer*/

// testing -----------



data class GrunnlagGenerelt(
    override val behandlingstema: Kodeverdi,
    override val identdato: LocalDate,
    override val periode: Periode?,
    override val arbeidskategori: Kodeverdi?,
    override val arbeidsforhold: List<Arbeidsforhold>,
    override val vedtak: List<Vedtak>
) : Grunnlag

data class Foreldrepenger(
    private val generelt: GrunnlagGenerelt,
    val opprinneligIdentdato: LocalDate,
    val dekningsgrad: Int?,
    val gradering: Int?,
    val foedselsdatoBarn: LocalDate
) : Grunnlag by generelt
