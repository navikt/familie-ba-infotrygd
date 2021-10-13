package no.nav.familie.ba.infotrygd.model.converters

import javax.persistence.Converter

@Converter
class NavReversedLocalDateConverter : AbstractNavLocalDateConverter("ddMMyyyy")