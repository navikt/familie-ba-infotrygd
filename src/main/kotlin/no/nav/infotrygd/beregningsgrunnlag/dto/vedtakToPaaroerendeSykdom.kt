package no.nav.infotrygd.beregningsgrunnlag.dto

import no.nav.infotrygd.beregningsgrunnlag.model.db2.Vedtak
import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Stoenadstype
import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Tema

fun vedtakToPaaroerendeSykdom(vedtak: Vedtak): PaaroerendeSykdom {
    vedtak.delytelser.forEach { require(it.type == "PN") { "Ugyldig databaseverdi, forventet type = PN" } }

    val delytelser = vedtak.delytelser.sortedBy { it.fom }
    val periode = Periode(vedtak.stonad.datoStart, delytelser.last().tom)

    return PaaroerendeSykdom(
        generelt = GrunnlagGenerelt(
            tema = Tema.PAAROERENDE_SYKDOM.toDto(),
            registrert = null,
            status = null,
            saksbehandlerId = vedtak.stonad.stonadBs?.brukerId,
            iverksatt = vedtak.stonad.datoStart,
            opphoerFom = vedtak.stonad.datoOpphoer,
            behandlingstema = Stoenadstype.PLEIEPENGER_NY_ORDNING.toDto(),
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