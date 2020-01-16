package no.nav.infotrygd.beregningsgrunnlag.dto

fun periodeToGrunnlag(p: no.nav.infotrygd.beregningsgrunnlag.model.Periode): GrunnlagGenerelt {
    val tema = p.tema
    val status = p.frisk.status?.let { Kodeverdi(it.kode, it.tekst) }

    val utbetaltFom = p.utbetaltFom
    val utbetaltTom = p.utbetaltTom

    val periode: Periode? = if(utbetaltFom != null && utbetaltTom != null)
        Periode(utbetaltFom, utbetaltTom) else null

    val arbeidskategori: Kodeverdi? = {
        val kat = p.arbeidskategori
        if (kat == null) {
            null
        } else {
            Kodeverdi(kat.kode, kat.tekst)
        }
    }()

    return GrunnlagGenerelt(
        tema = Kodeverdi(tema.kode, tema.tekst),
        registrert = p.registrert,
        status = status,
        saksbehandlerId = p.brukerId,
        iverksatt = p.arbufoer,
        opphoerFom = p.opphoerFom,
        behandlingstema = p.stoenadstype!!.toDto(),
        identdato = p.arbufoer,
        periode = periode,
        arbeidskategori = arbeidskategori,
        arbeidsforhold = p.inntekter.map {
            Arbeidsforhold(
                inntektForPerioden = it.loenn,
                inntektsperiode = Kodeverdi(it.periode.kode, it.periode.tekst),
                arbeidsgiverOrgnr = it.arbgiverNr,
                refusjon = it.refusjon
            )
        },
        vedtak = p.utbetalinger.map {
            Vedtak(
                utbetalingsgrad = it.grad ?: 100,
                periode = Periode(
                    it.utbetaltFom,
                    it.utbetaltTom
                )
            )
        }
    )
}