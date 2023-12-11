package no.nav.familie.ba.infotrygd.model.dl1

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.familie.ba.infotrygd.model.converters.ReversedFoedselNrConverter
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "SA_HENDELSE_20")
data class Hendelse(
    @Id
    @Column(name = "ID_HEND", nullable = false, columnDefinition = "DECIMAL")
    val id: Long,

    @Column(name = "S01_PERSONKEY", columnDefinition = "DECIMAL")
    val personKey: Long,

    @Column(name = "S05_SAKSBLOKK", columnDefinition = "CHAR")
    val saksblokk: String,

    @Column(name = "S20_SAKSNR", columnDefinition = "CHAR")
    val saksnummer: String,

    @Column(name = "S20_AKSJONSDATO_SEQ", columnDefinition = "DECIMAL")
    val aksjonsdatoSeq: Long,

    @Column(name = "S20_TEKSTKODE_1", columnDefinition = "VARCHAR2")
    val tekstKode1: String,

    @Column(name = "F_NR", columnDefinition = "VARCHAR2")
    @Convert(converter = ReversedFoedselNrConverter::class)
    val fnr: FoedselsNr,

    @Column(name = "TK_NR", columnDefinition = "VARCHAR2")
    val tkNr: String,

    @Column(name = "REGION", columnDefinition = "CHAR")
    val region: String

)