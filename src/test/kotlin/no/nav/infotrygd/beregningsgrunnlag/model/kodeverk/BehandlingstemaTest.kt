package no.nav.infotrygd.beregningsgrunnlag.model.kodeverk

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class BehandlingstemaTest {

    @Test
    fun toDto() {
        val map = mapOf(
            "" to "SP",
            "FP" to "FØ",
            "Z" to "SV")

        val mapKeys: MutableSet<String> = HashSet()

        for(v in Behandlingstema.values()) {
            val dto = v.toDto()
            if(v.kode in map) {
                assertThat(dto.kode).isEqualTo(map[v.kode])
                mapKeys.add(v.kode)
            } else {
                assertThat(dto.kode).isEqualTo(v.kode)
                assertThat(dto.termnavn).isEqualTo(v.tekst)
            }
        }

        assertThat(mapKeys).isEqualTo(map.keys)
    }
}