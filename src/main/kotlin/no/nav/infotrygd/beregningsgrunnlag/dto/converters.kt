package no.nav.infotrygd.beregningsgrunnlag.dto

import no.nav.infotrygd.beregningsgrunnlag.model.VedtakBarn
import no.nav.infotrygd.beregningsgrunnlag.model.Ytelse

fun periodeToForeldrepenger(p: no.nav.infotrygd.beregningsgrunnlag.model.Periode, vedtak: VedtakBarn?): Foreldrepenger {
    check(p.ytelse == Ytelse.FORELDREPENGER) { "Forventet ytelse == FORELDREPENGER" }
    checkNotNull(p.foedselsdatoBarn) { "foedselsdatoBarn kan ikke være null for foreldrepenger" }

    return Foreldrepenger(
        generelt = periodeToGrunnlag(p),
        opprinneligIdentdato = p.arbufoerOpprinnelig,
        dekningsgrad = p.dekningsgrad,
        gradering = vedtak?.dekningsgrad,
        foedselsdatoBarn = p.foedselsdatoBarn!!
    )
}

fun periodeToGrunnlag(p: no.nav.infotrygd.beregningsgrunnlag.model.Periode): GrunnlagGenerelt {

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
        behandlingstema = p.stoenadstype!!.toBehandlingstema(),
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