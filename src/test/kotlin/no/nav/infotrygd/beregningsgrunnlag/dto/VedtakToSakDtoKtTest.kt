package no.nav.infotrygd.beregningsgrunnlag.dto

import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Stoenadstype
import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Tema
import no.nav.infotrygd.beregningsgrunnlag.testutil.TestData
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

internal class VedtakToSakDtoKtTest {

    @Test
    fun toDto() {
        val vedtatt = LocalDate.now()
        val iverksatt = vedtatt.plusDays(1)
        val opphoerFom = iverksatt.plusDays(1)

        val registrert = vedtatt.atStartOfDay()

        val vedtak = TestData.vedtak(
            datoStart = iverksatt,
            kodeRutine = "BS",
            tidspunktRegistrert = registrert,
            datoOpphoer = opphoerFom
        )

        val forventet = SakDto(
            sakId = null,
            tema = Tema.PAAROERENDE_SYKDOM.toDto(),
            behandlingstema = Stoenadstype.PLEIEPENGER_NY_ORDNING.toDto(),
            type = null,
            status = null,
            resultat = null,
            vedtatt = vedtatt,
            iverksatt = iverksatt,
            registrert = registrert.toLocalDate(),
            opphoerFom = opphoerFom
        )

        assertThat(vedtakToSakDto(vedtak)).isEqualTo(forventet)
    }
}