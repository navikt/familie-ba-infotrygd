package no.nav.infotrygd.beregningsgrunnlag.dto

import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.*
import no.nav.infotrygd.beregningsgrunnlag.model.sak.Sak
import no.nav.infotrygd.beregningsgrunnlag.model.sak.Status
import no.nav.infotrygd.beregningsgrunnlag.testutil.TestData
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

internal class SakToSakDtoKtTest {
    @Test
    fun sakToSakDto() {
        val tema = Tema.PAAROERENDE_SYKDOM.toDto()
        val behandlingstema = SakValg.PN
        val type = SakType.A
        val status = SakStatus.FB
        val resultat = SakResultat.BESLUTNINGSSTOETTE
        val vedtatt = LocalDate.now()
        val iverksatt = vedtatt.minusDays(1)

        val sak = Sak(
            id = -1,
            fnr = TestData.foedselsNr(),
            personKey = -1,
            saksblokk = "B",
            saksnummer = "52",
            kapittelNr = "BS",
            valg = behandlingstema,
            type = type,
            resultat = resultat,
            vedtaksdato = vedtatt,
            iverksattdato = iverksatt,
            statushistorikk = listOf(
                Status(
                    id = -1,
                    personKey = -1,
                    saksblokk = "B",
                    saksnummer = "52",
                    lopeNr = 1,
                    status = status
                )
            )
        )

        val forventet = SakDto(
            sakId = "B52",
            tema = tema,
            behandlingstema = behandlingstema.toDto(),
            type = type.toDto(),
            status = status.toDto(),
            resultat = resultat.toDto(),
            vedtatt = vedtatt,
            iverksatt = iverksatt,
            opphoerFom = null
        )

        assertThat(sakToSakDto(sak)).isEqualTo(forventet)
    }
}