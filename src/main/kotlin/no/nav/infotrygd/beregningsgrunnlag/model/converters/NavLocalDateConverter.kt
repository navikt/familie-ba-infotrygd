package no.nav.infotrygd.beregningsgrunnlag.model.converters

import javax.persistence.Converter

@Converter
class NavLocalDateConverter : AbstractNavLocalDateConverter("yyyyMMdd")