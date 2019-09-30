package no.nav.infotrygd.beregningsgrunnlag.dto

import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Arbeidskategori
import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Inntektsperiode
import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Stoenadstype
import no.nav.infotrygd.beregningsgrunnlag.testutil.TestData
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

class ConvertersKtTest {

    @Test
    fun periodeToGrunnlag() {
        val utbetaltTom = LocalDate.now()
        val utbetaltFom = utbetaltTom.minusMonths(1)
        val stoenadstype = Stoenadstype.FOEDSEL
        val inntektsperiode = Inntektsperiode.MAANEDLIG
        val arbeidsgiverOrgnr = "12345678900"
        val inntektForPerioden = 1000.toBigDecimal()
        val arbeidskategori = Arbeidskategori.AMBASSADEPERSONELL
        val utbetalingsgrad = 100

        val pf = TestData.PeriodeFactory()

        val inntekt = pf.inntekt().copy(
            periode = inntektsperiode,
            arbgiverNr = arbeidsgiverOrgnr,
            loenn = inntektForPerioden
        )

        val utbetaling = pf.utbetaling().copy(
            utbetaltFom = utbetaltFom,
            utbetaltTom = utbetaltTom,
            grad = utbetalingsgrad
        )

        val periode = pf.periode().copy(
            arbufoer = utbetaltFom,
            stoenadstype = stoenadstype,
            utbetaltFom = utbetaltFom,
            utbetaltTom = utbetaltTom,
            arbeidskategori = arbeidskategori,
            inntekter = listOf(inntekt),
            utbetalinger = listOf(utbetaling)
        )

        val dto = periodeToGrunnlag(periode)
        val forventet = GrunnlagGenerelt(
            behandlingstema = stoenadstype.toBehandlingstema(),
            identdato = utbetaltFom,
            periode = Periode(utbetaltFom, utbetaltTom),
            arbeidskategori = Kodeverdi(arbeidskategori.kode, arbeidskategori.tekst),
            arbeidsforhold = listOf(
                Arbeidsforhold(
                    inntektForPerioden = inntektForPerioden,
                    inntektsperiode = Kodeverdi(inntektsperiode.kode, inntektsperiode.tekst),
                    arbeidsgiverOrgnr = arbeidsgiverOrgnr
                )
            ),
            vedtak = listOf(
                Vedtak(
                    utbetalingsgrad = utbetalingsgrad,
                    periode = Periode(utbetaltFom, utbetaltTom)
                )
            )
        )

        assertThat(dto).isEqualTo(forventet)
    }

    @Test
    fun periodeToForeldrepengerDetaljer() {
        val stoenadstype = Stoenadstype.FOEDSEL // ytelse = foreldrepenger
        val opprinneligIdentdato = LocalDate.now()
        val dekningsgrad = 75
        val gradering = 50
        val foedselsdatoBarn = LocalDate.now().minusYears(1)

        val pf = TestData.PeriodeFactory()

        val vedtak = pf.vedtakBarn().copy(
            dekningsgrad = gradering
        )

        val periode = pf.periode().copy(
            stoenadstype = stoenadstype,
            arbufoerOpprinnelig = opprinneligIdentdato,
            dekningsgrad = dekningsgrad,
            foedselsdatoBarn = foedselsdatoBarn
        )

        val dto = periodeToForeldrepengerDetaljer(periode, vedtak)

        val expected = ForeldrepengerDetaljer(
            opprinneligIdentdato = opprinneligIdentdato,
            dekningsgrad = dekningsgrad,
            gradering = gradering,
            foedselsdatoBarn = foedselsdatoBarn
        )

        assertThat(dto).isEqualTo(expected)
    }
}