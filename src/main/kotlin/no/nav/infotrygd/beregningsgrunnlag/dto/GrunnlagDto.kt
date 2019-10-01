package no.nav.infotrygd.beregningsgrunnlag.dto

import java.math.BigDecimal
import java.time.LocalDate

interface Grunnlag {
    val tema: Kodeverdi?
    val registrert: LocalDate?
    val status: Kodeverdi?
    val saksbehandlerId: String?
    val iverksatt: LocalDate?
    val opphoerFom: LocalDate?
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

/*
data class PaarorendeSykdom(
    val foedselsdatoPleietrengende: LocalDate
) : Grunnlagsdetaljer*/

data class GrunnlagGenerelt(
    override val tema: Kodeverdi?,
    override val registrert: LocalDate?,
    override val status: Kodeverdi?,
    override val saksbehandlerId: String?,
    override val iverksatt: LocalDate?,
    override val opphoerFom: LocalDate?,
    override val behandlingstema: Kodeverdi,
    override val identdato: LocalDate,
    override val periode: Periode?,
    override val arbeidskategori: Kodeverdi?,
    override val arbeidsforhold: List<Arbeidsforhold>,
    override val vedtak: List<Vedtak>
) : Grunnlag


// ======================================== Ytelser ========================================


// --- Foreldrepenger ---

interface Foreldrepengerfelt {
    val opprinneligIdentdato: LocalDate
    val dekningsgrad: Int?
    val gradering: Int?
    val foedselsdatoBarn: LocalDate
}

data class ForeldrepengerDetaljer(
    override val opprinneligIdentdato: LocalDate,
    override val dekningsgrad: Int?,
    override val gradering: Int?,
    override val foedselsdatoBarn: LocalDate
) : Foreldrepengerfelt

data class Foreldrepenger(
    private val generelt: GrunnlagGenerelt,
    private val foreldrepengerDetaljer: ForeldrepengerDetaljer
) : Grunnlag by generelt,
    Foreldrepengerfelt by foreldrepengerDetaljer



// --- Sykepenger ---

data class Sykepenger(
    private val generelt: GrunnlagGenerelt,
    val inntektsgrunnlagProsent: Int?
) : Grunnlag by generelt