package no.nav.infotrygd.beregningsgrunnlag.dto

import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Stoenadstype
import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Tema

fun vedtakToSakDto(vedtak: no.nav.infotrygd.beregningsgrunnlag.model.db2.Vedtak): SakDto {
    return SakDto(
        sakId = null,
        tema = Tema.PAAROERENDE_SYKDOM.toDto(),
        behandlingstema = Stoenadstype.PLEIEPENGER_NY_ORDNING.toDto(),
        type = null,
        status = null,
        resultat = null,
        vedtatt = vedtak.stonad.tidspunktRegistrert.toLocalDate(),
        iverksatt = vedtak.stonad.datoStart,
        opphoerFom = vedtak.stonad.datoOpphoer
    )
}