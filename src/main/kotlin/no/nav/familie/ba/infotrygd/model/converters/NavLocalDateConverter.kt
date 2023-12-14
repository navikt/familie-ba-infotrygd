package no.nav.familie.ba.infotrygd.model.converters

import jakarta.persistence.Converter

@Converter
class NavLocalDateConverter : AbstractNavLocalDateConverter("yyyyMMdd")