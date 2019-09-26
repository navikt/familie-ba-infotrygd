package no.nav.infotrygd.beregningsgrunnlag.values

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class FodselNrTest {
    @Test
    fun kjoenn() {
        val mann = FodselNr("00000000100")
        val kvinne = FodselNr("00000000200")

        assertThat(mann.kjoenn).isEqualTo(Kjoenn.MANN)
        assertThat(kvinne.kjoenn).isEqualTo(Kjoenn.KVINNE)
    }
}