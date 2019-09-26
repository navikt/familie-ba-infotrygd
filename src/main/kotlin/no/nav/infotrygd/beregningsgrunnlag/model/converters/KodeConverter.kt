package no.nav.infotrygd.beregningsgrunnlag.model.converters

import no.nav.infotrygd.beregningsgrunnlag.exception.UkjentDatabaseverdiException
import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Arbeidskategori
import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Inntektsperiode
import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Stoenadstype
import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Kode
import javax.persistence.AttributeConverter
import javax.persistence.Converter

abstract class KodeConverter<T : Kode>(private val koder: List<T>, val fieldSize: Int = 0, val padChar: Char = ' ') : AttributeConverter<T?, String?> {

    override fun convertToDatabaseColumn(attribute: T?): String? {
        return attribute?.kode?.padEnd(length = fieldSize, padChar = padChar)
    }

    override fun convertToEntityAttribute(dbData: String?): T? {
        if(dbData.isNullOrBlank()) {
            return null
        }

        val normalisertKode = dbData.trim()
        for(verdi in koder) {
            if (verdi.kode == normalisertKode) {
                return verdi
            }
        }

        throw UkjentDatabaseverdiException(normalisertKode, koder.map { it.kode })
    }
}

@Converter(autoApply = true)
class BehandlingstemaConverter : KodeConverter<Stoenadstype>(Stoenadstype.values().toList(), fieldSize = 2)

@Converter(autoApply = true)
class ArbeidskategoriConverter : KodeConverter<Arbeidskategori>(Arbeidskategori.values().toList(), fieldSize = 2)

@Converter(autoApply = true)
class InntektsperiodeConverter : KodeConverter<Inntektsperiode>(Inntektsperiode.values().toList(), fieldSize = 2)