package no.nav.infotrygd.beregningsgrunnlag.model.kodeverk

enum class KodeRutine(override val kode: String, override val tekst: String) : Kode {
    BS("BS", "Barn Sykdom"),
    BR("BR", "Barn Sykdom m/refusjon"),
    TEST("~~", "test")
}