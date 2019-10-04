package no.nav.infotrygd.beregningsgrunnlag.dto

import no.nav.infotrygd.beregningsgrunnlag.model.db2.Vedtak
import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Stoenadstype

fun vedtakToPaaroerendeSykdom(vedtak: Vedtak): PaaroerendeSykdom {
    val stoenadstype = Stoenadstype.PLEIEPENGER_NY_ORDNING
    vedtak.delytelser.forEach { require(it.type == stoenadstype) { "Ugyldig databaseverdi, forventet type = PN" } }

    val delytelser = vedtak.delytelser.sortedBy { it.fom }
    val periode = Periode(vedtak.stonad.datoStart, delytelser.last().tom)

    return PaaroerendeSykdom(
        generelt = GrunnlagGenerelt(
            tema = stoenadstype.tema.toDto(),
            registrert = null,
            status = null,
            saksbehandlerId = vedtak.stonad.stonadBs?.brukerId,
            iverksatt = vedtak.stonad.datoStart,
            opphoerFom = vedtak.stonad.datoOpphoer,
            behandlingstema = stoenadstype.toDto(),
            identdato = vedtak.stonad.datoStart,
            periode = periode,
            arbeidskategori = null, // todo: finn ut
            arbeidsforhold = listOf(), // todo: finn ut
            vedtak = delytelser.map {
                Vedtak(
                    utbetalingsgrad = it.delytelseSpFaBs?.grad,
                    periode = Periode(it.fom, it.tom)
                )
            }
        ),
        foedselsdatoPleietrengende = null
    )
}