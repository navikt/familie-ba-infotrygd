package no.nav.infotrygd.beregningsgrunnlag.dto

import no.nav.infotrygd.beregningsgrunnlag.model.db2.Vedtak
import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Stoenadstype
import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Tema

fun vedtakToPaaroerendeSykdom(vedtak: Vedtak): PaaroerendeSykdom {
    vedtak.delytelser.forEach { require(it.type == "PN") { "Ugyldig databaseverdi, forventet type = PN" } }

    val delytelser = vedtak.delytelser.sortedBy { it.fom }
    val datoStart = vedtak.stonad.datoStart
    val periode = Periode(datoStart, delytelser.last().tom)

    return PaaroerendeSykdom(
        generelt = GrunnlagGenerelt(
            tema = Tema.PAAROERENDE_SYKDOM.toDto(),
            registrert = vedtak.stonad.stonadBs?.tidspunktRegistrert?.toLocalDate(),
            status = null,
            saksbehandlerId = vedtak.stonad.stonadBs?.brukerId,
            iverksatt = datoStart,
            opphoerFom = vedtak.stonad.datoOpphoer,
            behandlingstema = Stoenadstype.PLEIEPENGER_NY_ORDNING.toDto(),
            identdato = datoStart,
            periode = periode,
            arbeidskategori = vedtak.vedtakSpFaBs?.arbeidskategori?.toDto(),
            arbeidsforhold = vedtak.stonad.inntekter.map {
                Arbeidsforhold(
                    inntektForPerioden = it.inntekt,
                    inntektsperiode = it.periode.toDto(),
                    arbeidsgiverOrgnr = it.orgNr.toString()
                )
            },
            vedtak = delytelser.map {
                Vedtak(
                    utbetalingsgrad = it.delytelseSpFaBs?.grad,
                    periode = Periode(it.fom, it.tom)
                )
            }
        ),
        foedselsdatoPleietrengende = vedtak.stonad.stonadBs?.barn?.fnr?.foedselsdato
    )
}