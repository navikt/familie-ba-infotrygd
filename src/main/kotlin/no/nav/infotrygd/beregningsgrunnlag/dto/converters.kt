package no.nav.infotrygd.beregningsgrunnlag.dto

import no.nav.infotrygd.beregningsgrunnlag.model.VedtakBarn
import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Tema

fun periodeToForeldrepengerDetaljer(p: no.nav.infotrygd.beregningsgrunnlag.model.Periode, vedtak: VedtakBarn?): ForeldrepengerDetaljer {
    check(p.tema == Tema.FORELDREPENGER) { "Forventet ytelse == FORELDREPENGER" }

    return ForeldrepengerDetaljer(
        opprinneligIdentdato = p.arbufoerOpprinnelig,
        dekningsgrad = p.dekningsgrad,
        gradering = vedtak?.dekningsgrad,
        foedselsdatoBarn = p.foedselsdatoBarn
    )
}

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
        registrert = p.regdato,
        status = status,
        saksbehandlerId = p.brukerId,
        iverksatt = p.arbufoer,
        opphoerFom = p.stoppdato,
        behandlingstema = p.stoenadstype!!.toDto(),
        identdato = p.arbufoer, // todo: pårørende sykdom
        periode = periode, // todo: pårørende sykdom
        arbeidskategori = arbeidskategori,
        arbeidsforhold = p.inntekter.map {
            Arbeidsforhold(
                inntektForPerioden = it.loenn, // todo: pårørende sykdom
                inntektsperiode = Kodeverdi(it.periode.kode, it.periode.tekst),
                arbeidsgiverOrgnr = it.arbgiverNr
            )
        },
        vedtak = p.utbetalinger.map {
            Vedtak(
                utbetalingsgrad = it.grad,
                periode = Periode(
                    it.utbetaltFom,
                    it.utbetaltTom
                ) // todo: pårørende sykdom
            )
        }
    )
}