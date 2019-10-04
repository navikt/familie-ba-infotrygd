package no.nav.infotrygd.beregningsgrunnlag.dto

import no.nav.infotrygd.beregningsgrunnlag.model.db2.*
import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.KodeRutine
import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Stoenadstype
import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Tema
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

        val vedtak = TVedtak(
            id = -1,
            stonad = Stonad(
                id = nextId(),
                kodeRutine = KodeRutine.BS,
                datoStart = iverksatt,
                datoOpphoer = opphoerFom,
                stonadBs = StonadBs(
                    id = -1,
                    brukerId = "bruker"
                )
            ),
            person = LopenrFnr(
                id = -1,
                fnr = TestData.foedselNr()
            ),
            datoStart = LocalDate.now(),
            delytelser = listOf(
                Delytelse(
                    vedtakId = -1,
                    type = Stoenadstype.PLEIEPENGER_NY_ORDNING,
                    tidspunktRegistrert = LocalDateTime.now(),
                    fom = fom1,
                    tom = tom1,
                    delytelseSpFaBs = DelytelseSpFaBs(
                        vedtakId = -1,
                        type = Stoenadstype.PLEIEPENGER_NY_ORDNING,
                        tidspunktRegistrert = LocalDateTime.now(),
                        grad = 75
                    )
                ),
                Delytelse(
                    vedtakId = -1,
                    type = Stoenadstype.PLEIEPENGER_NY_ORDNING,
                    tidspunktRegistrert = LocalDateTime.now(),
                    fom = fom2,
                    tom = tom2,
                    delytelseSpFaBs = DelytelseSpFaBs(
                        vedtakId = -1,
                        type = Stoenadstype.PLEIEPENGER_NY_ORDNING,
                        tidspunktRegistrert = LocalDateTime.now(),
                        grad = 65
                    )
                )
            ),
            kodeRutine = KodeRutine.BS
        )

        val forventet = PaaroerendeSykdom(
            generelt = GrunnlagGenerelt(
                tema = Kodeverdi("BS", "Barns sykdom"),
                registrert = null,
                status = null,
                saksbehandlerId = "bruker",
                iverksatt = iverksatt,
                opphoerFom = opphoerFom,
                behandlingstema = Kodeverdi("PN", "Pleiepenger, ny ordning (identdato etter 1.10.2017)"),
                identdato = iverksatt,
                periode = Periode(iverksatt, periodeTom),
                arbeidskategori = null, // todo- finn ut
                arbeidsforhold = listOf(), // todo- finn ut
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
            foedselsdatoPleietrengende = null
        )

        val resultat = vedtakToPaaroerendeSykdom(vedtak)
        assertThat(resultat).isEqualTo(forventet)
    }
}