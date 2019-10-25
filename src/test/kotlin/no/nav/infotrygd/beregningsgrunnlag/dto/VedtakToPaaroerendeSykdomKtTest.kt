package no.nav.infotrygd.beregningsgrunnlag.dto

import no.nav.infotrygd.beregningsgrunnlag.model.db2.*
import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Arbeidskategori
import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Inntektsperiode
import no.nav.infotrygd.beregningsgrunnlag.nextId
import no.nav.infotrygd.beregningsgrunnlag.testutil.TestData
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import no.nav.infotrygd.beregningsgrunnlag.model.db2.Vedtak as TVedtak

class VedtakToPaaroerendeSykdomKtTest {

    @Test
    fun vedtakToPaaroerendeSykdom() {
        val iverksatt = LocalDate.now()
        val opphoerFom = LocalDate.now()
        val periodeTom = LocalDate.now().plusMonths(1)
        val fom1 = iverksatt
        val tom1 = fom1.plusDays(1)

        val fom2 = fom1.plusWeeks(1)
        val tom2 = periodeTom

        val arbeidskategori = Arbeidskategori.AMBASSADEPERSONELL
        val inntektForPerioden = 1000.toBigDecimal()
        val inntektsperiode = Inntektsperiode.MAANEDLIG
        val arbeidsgiverOrgNr = 12345678900
        val tidspunktRegistrert = iverksatt.minusDays(12).atStartOfDay()

        val barnFnr = TestData.foedselsNr(foedselsdato = LocalDate.of(2003, 2, 1))
        val refusjon = true

        val vedtak = TVedtak(
            id = -1,
            stonad = Stonad(
                id = nextId(),
                kodeRutine = "BS",
                datoStart = iverksatt,
                datoOpphoer = opphoerFom,
                stonadBs = StonadBs(
                    id = -1,
                    brukerId = "bruker",
                    tidspunktRegistrert = tidspunktRegistrert,
                    barn = LopenrFnr(
                        id = nextId(),
                        fnr = barnFnr
                    )
                ),
                inntektshistorikk = listOf(
                    Inntekt(
                        stonadId = -1,
                        orgNr = arbeidsgiverOrgNr,
                        inntektFom = LocalDate.now(),
                        lopeNr = 1,
                        inntekt = inntektForPerioden,
                        periode = inntektsperiode,
                        status = "L",
                        refusjon = refusjon
                    )
                )
            ),
            person = LopenrFnr(
                id = -1,
                fnr = TestData.foedselsNr()
            ),
            datoStart = LocalDate.now(),
            vedtakSpFaBs = VedtakSpFaBs(
                vedtakId = -1,
                arbeidskategori = arbeidskategori
            ),
            delytelser = listOf(
                Delytelse(
                    vedtakId = -1,
                    type = "PN",
                    tidspunktRegistrert = LocalDateTime.now(),
                    fom = fom1,
                    tom = tom1,
                    delytelseSpFaBs = DelytelseSpFaBs(
                        vedtakId = -1,
                        type = "PN",
                        tidspunktRegistrert = LocalDateTime.now(),
                        grad = 75
                    )
                ),
                Delytelse(
                    vedtakId = -1,
                    type = "PN",
                    tidspunktRegistrert = LocalDateTime.now(),
                    fom = fom2,
                    tom = tom2,
                    delytelseSpFaBs = DelytelseSpFaBs(
                        vedtakId = -1,
                        type = "PN",
                        tidspunktRegistrert = LocalDateTime.now(),
                        grad = 65
                    )
                )
            ),
            kodeRutine = "BS"
        )

        val forventet = PaaroerendeSykdom(
            generelt = GrunnlagGenerelt(
                tema = Kodeverdi("BS", "Barns sykdom"),
                registrert = tidspunktRegistrert.toLocalDate(),
                status = null,
                saksbehandlerId = "bruker",
                iverksatt = iverksatt,
                opphoerFom = opphoerFom,
                behandlingstema = Kodeverdi("PN", "Pleiepenger, ny ordning (identdato etter 1.10.2017)"),
                identdato = iverksatt,
                periode = Periode(iverksatt, periodeTom),
                arbeidskategori = arbeidskategori.toDto(),
                arbeidsforhold = listOf(
                    Arbeidsforhold(
                        inntektForPerioden = inntektForPerioden,
                        inntektsperiode = inntektsperiode.toDto(),
                        arbeidsgiverOrgnr = arbeidsgiverOrgNr.toString(),
                        refusjon = refusjon
                    )
                ),
                vedtak = listOf(
                    Vedtak(
                        utbetalingsgrad = 75,
                        periode = Periode(fom1, tom1)
                    ),
                    Vedtak(
                        utbetalingsgrad = 65,
                        periode = Periode(fom2, tom2)
                    )
                )
            ),
            foedselsdatoPleietrengende = null,
            foedselsnummerPleietrengende = barnFnr.asString
        )

        val resultat = vedtakToPaaroerendeSykdom(vedtak)
        assertThat(resultat).isEqualTo(forventet)
    }
}