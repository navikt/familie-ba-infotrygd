package no.nav.infotrygd.beregningsgrunnlag.rest.dto

import java.time.LocalDate

data class Grunnlag(
    val behandlingstema: Kodeverdi, // todo: kodeverk
    val identdato: LocalDate,
    val periode: Periode,
    val arbeidskategori: Kodeverdi, // todo: kodeverk
    val arbeidsforhold: List<Arbeidsforhold>,

    val detaljer: Grunnlagsdetaljer
)


data class Vedtak(
    val utbetalingsgrad: Int,
    val periode: Periode
)

data class Periode(
    val fom: LocalDate,
    val tom: LocalDate
)

data class Arbeidsforhold(
    val inntektForPerioden: Long,
    val inntektsperiode: Kodeverdi,
    val orgNr: String
)

interface Grunnlagsdetaljer

data class Foreldrepenger(
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
) : Grunnlagsdetaljer

data class Kodeverdi(val kode: String, val termnavn: String)