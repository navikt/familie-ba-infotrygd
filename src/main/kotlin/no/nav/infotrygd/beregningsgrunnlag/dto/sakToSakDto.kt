package no.nav.infotrygd.beregningsgrunnlag.dto

import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Tema
import no.nav.infotrygd.beregningsgrunnlag.model.sak.Sak

fun sakToSakDto(sak: Sak): SakDto {
    require(sak.kapittelNr == "BS") {"Kapittelnr. må være BS"}

    return SakDto(
        sakId = "${sak.saksblokk}${sak.saksnummer}",
        tema = Tema.PAAROERENDE_SYKDOM.toDto(),
        behandlingstema = sak.valg.toDto(),
        type = sak.type.toDto(),
        status = sak.status.toDto(),
        resultat = sak.resultat.toDto(),
        vedtatt = sak.vedtaksdato,
        iverksatt = sak.iverksattdato,
        opphoerFom = null
    )
}