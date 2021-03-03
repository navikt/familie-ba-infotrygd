package no.nav.infotrygd.barnetrygd.model

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.infotrygd.barnetrygd.model.converters.*
import no.nav.infotrygd.barnetrygd.model.kodeverk.SakStatus
import no.nav.familie.kontrakter.ba.infotrygd.Sak as SakDto
import org.hibernate.annotations.Cascade
import org.hibernate.annotations.CascadeType
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import java.io.Serializable
import java.time.LocalDate
import javax.persistence.*

@Entity
@Table(name = "SA_SAK_10")
data class Sak(
    @Id
    @Column(name = "ID_SAK", columnDefinition = "DECIMAL", nullable = false)
    val id: Long,

    @Column(name = "REGION", columnDefinition = "CHAR")
    val region: String,

    @Column(name = "S01_PERSONKEY", columnDefinition = "DECIMAL")
    val personKey: Long,

    @ManyToOne
    @JoinColumns(value = [
        JoinColumn(name = "S01_PERSONKEY", referencedColumnName = "S01_PERSONKEY", insertable= false, updatable = false),
        JoinColumn(name = "REGION", referencedColumnName = "REGION", insertable= false, updatable = false),
    ])
    @Cascade(value = [CascadeType.MERGE])
    val person: Person,

    @Column(name = "S05_SAKSBLOKK", columnDefinition = "CHAR")
    val saksblokk: String,

    @Column(name = "S10_SAKSNR", columnDefinition = "CHAR")
    val saksnummer: String,

    @Column(name = "S10_KAPITTELNR", columnDefinition = "CHAR")
    val kapittelNr: String,

    @Column(name = "S10_VALG", columnDefinition = "CHAR")
    @Convert(converter = Char2Converter::class)
    val valg: String,

    @Column(name = "S10_TYPE", columnDefinition = "CHAR")
    @Convert(converter = Char2Converter::class)
    val type: String,

    @Column(name = "S10_UNDERVALG", columnDefinition = "CHAR")
    @Convert(converter = Char2Converter::class)
    val undervalg: String,

    @Column(name = "S10_RESULTAT", columnDefinition = "CHAR")
    @Convert(converter = Char2Converter::class)
    val resultat: String?,

    @OneToMany(fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    @JoinColumns(value = [
        JoinColumn(name = "REGION", referencedColumnName = "REGION"),
        JoinColumn(name = "B01_PERSONKEY", referencedColumnName = "S01_PERSONKEY"),
        JoinColumn(name = "B20_BLOKK", referencedColumnName = "S05_SAKSBLOKK"),
        JoinColumn(name = "B20_SAK_NR", referencedColumnName = "S10_SAKSNR")
    ])
    @Cascade(value = [CascadeType.MERGE])
    val stønadList: List<Stønad>,

    @Column(name = "S10_VEDTAKSDATO", columnDefinition = "DECIMAL")
    @Convert(converter = NavReversedLocalDateConverter::class)
    val vedtaksdato: LocalDate?,

    @Column(name = "S10_IVERKSATTDATO", columnDefinition = "DECIMAL")
    @Convert(converter = NavReversedLocalDateConverter::class)
    val iverksattdato: LocalDate?,

    @Column(name = "S10_REG_DATO", columnDefinition = "DECIMAL")
    @Convert(converter = NavReversedLocalDateConverter::class)
    val regDato: LocalDate?,

    @Column(name = "S10_MOTTATTDATO", columnDefinition = "DECIMAL")
    @Convert(converter = NavReversedLocalDateConverter::class)
    val mottattdato: LocalDate?,

    @Column(name = "S10_NIVAA", columnDefinition = "CHAR")
    @Convert(converter = Char3Converter::class)
    val nivaa: String? = null,

    @Column(name = "S10_AARSAKSKODE", columnDefinition = "CHAR")
    @Convert(converter = Char2Converter::class)
    val aarsakskode: String? = null,

    @Column(name = "S10_BEHEN_ENHET", columnDefinition = "CHAR(4)")
    val behenEnhet: String? = null,

    @Column(name = "S10_REG_AV_ENHET", columnDefinition = "CHAR(4)")
    val regAvEnhet: String? = null,

    @Column(name = "S10_VEDTATT_AV_ENHET", columnDefinition = "CHAR(4)")
    val vedtattAvEnhet: String? = null,

    @Column(name = "TK_NR", columnDefinition = "CHAR(4)")
    val tkNr: String? = null,

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumns(value = [
        JoinColumn(name = "REGION", referencedColumnName = "REGION"),
        JoinColumn(name = "S01_PERSONKEY", referencedColumnName = "S01_PERSONKEY"),
        JoinColumn(name = "S05_SAKSBLOKK", referencedColumnName = "S05_SAKSBLOKK"),
        JoinColumn(name = "S10_SAKSNR", referencedColumnName = "S10_SAKSNR")
    ])
    @Cascade(value = [CascadeType.ALL])
    val statushistorikk: List<Status>
) : Serializable {
    val status: SakStatus
        get() = statushistorikk.minByOrNull { it.lopeNr }?.status ?: SakStatus.IKKE_BEHANDLET

    @Entity
    @Table(name = "SA_PERSON_01")
    data class Person(
        @Id
        @Column(name = "ID_PERS", nullable = false, columnDefinition = "DECIMAL")
        val id: Long,

        @Column(name = "REGION", columnDefinition = "CHAR")
        val region: String,

        @Column(name = "S01_PERSONKEY", columnDefinition = "DECIMAL")
        val personKey: Long,

        @Column(name = "F_NR", columnDefinition = "CHAR")
        @Convert(converter = ReversedFoedselNrConverter::class)
        val fnr: FoedselsNr,

    ): Serializable
}

fun Sak.toSakDto(): SakDto {
    return SakDto(
        saksnr = this.saksnummer,
        saksblokk = this.saksblokk,
        regDato = this.regDato,
        mottattdato = this.mottattdato,
        kapittelnr = this.kapittelNr,
        valg = this.valg,
        undervalg = this.undervalg,
        type = this.type,
        nivå = this.nivaa,
        resultat = this.resultat,
        vedtaksdato = this.vedtaksdato,
        iverksattdato = this.iverksattdato,
        stønadList = this.stønadList.distinct().map { it.toStønadDto() },
        årsakskode = this.aarsakskode,
        behenEnhet = this.behenEnhet,
        regAvEnhet = this.regAvEnhet,
        status = this.status.kode,
    )
}