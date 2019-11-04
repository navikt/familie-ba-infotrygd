package no.nav.infotrygd.beregningsgrunnlag.dto

import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Frisk
import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Stoenadstype
import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Tema
import no.nav.infotrygd.beregningsgrunnlag.testutil.TestData
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

internal class PeriodeToSakDtoKtTest {
    @Test
    fun convert() {
        val frisk = Frisk.LOPENDE
        val vedtatt = LocalDate.now()
        val iverksatt = vedtatt.plusDays(1)
        val opphoerFom = iverksatt.plusDays(1)

        val periode = TestData.periode().copy(
            stoenadstype = Stoenadstype.BARNS_SYKDOM,
            arbufoer = iverksatt,
            stoppdato = opphoerFom,
            frisk = frisk
        )

        val forventet = SakDto(
            sakId = null,
            tema = Tema.PAAROERENDE_SYKDOM.toDto(),
            behandlingstema = Stoenadstype.BARNS_SYKDOM.toDto(),
            type = null,
            status = frisk.status?.toDto(),
            resultat = null,
            vedtatt = null,
            iverksatt = iverksatt,
            opphoerFom = opphoerFom
        )

        assertThat(periodeToSakDto(periode)).isEqualTo(forventet)
    }

    @Test
    fun opphoerFom() {
        var periode = TestData.periode()
        assertThat(periodeToSakDto(periode).opphoerFom).isNull()

        val stoppdato = LocalDate.of(2019, 1, 1)
        val friskmeldtDato = stoppdato.plusMonths(1)
        val arbufoerTom = friskmeldtDato.plusMonths(1)
        val maksdato = arbufoerTom.plusMonths(1)

        periode = periode.copy(maksdato = maksdato)
        assertThat(periodeToSakDto(periode).opphoerFom).isEqualTo(maksdato)

        periode = periode.copy(arbufoerTom = arbufoerTom)
        assertThat(periodeToSakDto(periode).opphoerFom).isEqualTo(arbufoerTom.plusDays(1))

        periode = periode.copy(friskmeldtDato = friskmeldtDato)
        assertThat(periodeToSakDto(periode).opphoerFom).isEqualTo(friskmeldtDato)

        periode = periode.copy(stoppdato = stoppdato)
        assertThat(periodeToSakDto(periode).opphoerFom).isEqualTo(stoppdato)
    }
}