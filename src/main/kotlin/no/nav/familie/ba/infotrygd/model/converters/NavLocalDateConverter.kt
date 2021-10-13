package no.nav.familie.ba.infotrygd.model.converters

import javax.persistence.Converter

@Converter
class NavLocalDateConverter : AbstractNavLocalDateConverter("yyyyMMdd")