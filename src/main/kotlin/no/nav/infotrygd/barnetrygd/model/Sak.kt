package no.nav.infotrygd.barnetrygd.model

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.infotrygd.barnetrygd.model.converters.*
import no.nav.infotrygd.barnetrygd.model.kodeverk.SakStatus
import org.hibernate.annotations.Cascade
import org.hibernate.annotations.CascadeType
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

    @Column(name = "S10_RESULTAT", columnDefinition = "CHAR")
    @Convert(converter = Char2Converter::class)
    val resultat: String?,

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

    @Column(name = "S10_DUBLETT_FEIL", columnDefinition = "CHAR")
    @Convert(converter = CharConverter::class)
    val dublettFeil: String? = null,

    @Column(name = "S10_INNSTILLING", columnDefinition = "CHAR")
    @Convert(converter = Char2Converter::class)
    val innstilling: String? = null,

    @Column(name = "S10_NIVAA", columnDefinition = "CHAR(3)")
    val nivaa: String? = null,

    @Column(name = "S10_INNSTILLDATO", columnDefinition = "DECIMAL")
    @Convert(converter = NavReversedLocalDateConverter::class)
    val innstilldato: LocalDate? = null,

    @Column(name = "S10_GRUNNBL_DATO", columnDefinition = "DECIMAL")
    @Convert(converter = NavReversedLocalDateConverter::class)
    val grunnblDato: LocalDate? = null,

    @Column(name = "S10_AARSAKSKODE", columnDefinition = "CHAR")
    @Convert(converter = Char2Converter::class)
    val aarsakskode: String? = null,

    @Column(name = "S10_TELLEPUNKT", columnDefinition = "CHAR(3)")
    val tellepunkt: String? = null,

    @Column(name = "S10_TELLETYPE", columnDefinition = "CHAR(1)")
    val telletype: String? = null,

    @Column(name = "S10_TELLEDATO", columnDefinition = "DECIMAL")
    @Convert(converter = NavLocalDateConverter::class)
    val telledato: LocalDate? = null,

    @Column(name = "S10_EVAL_KODE", columnDefinition = "CHAR(4)")
    val evalKode: String? = null,

    @Column(name = "S10_EVAL_TIR", columnDefinition = "CHAR(1)")
    val evalTir: String? = null,

    @Column(name = "S10_FREMLEGG", columnDefinition = "CHAR(3)")
    val fremlegg: String? = null,

    @Column(name = "S10_INNSTILLING2", columnDefinition = "CHAR(2)")
    val innstilling2: String? = null,

    @Column(name = "S10_INNSTILLDATO2", columnDefinition = "DECIMAL")
    @Convert(converter = NavReversedLocalDateConverter::class)
    val innstilldato2: LocalDate? = null,

    @Column(name = "S10_ANNEN_INSTANS", columnDefinition = "CHAR(1)")
    val annenInstans: String? = null,

    @Column(name = "S10_BEHEN_TYPE", columnDefinition = "CHAR(3)")
    val behenType: String? = null,

    @Column(name = "S10_BEHEN_ENHET", columnDefinition = "CHAR(4)")
    val behenEnhet: String? = null,

    @Column(name = "S10_REG_AV_TYPE", columnDefinition = "CHAR(3)")
    val regAvType: String? = null,

    @Column(name = "S10_REG_AV_ENHET", columnDefinition = "CHAR(4)")
    val regAvEnhet: String? = null,

    @Column(name = "S10_DIFF_FRAMLEGG", columnDefinition = "CHAR(3)")
    val diffFramlegg: String? = null,

    @Column(name = "S10_INNSTILLT_AV_TYPE", columnDefinition = "CHAR(3)")
    val innstilltAvType: String? = null,

    @Column(name = "S10_INNSTILLT_AV_ENHET", columnDefinition = "CHAR(4)")
    val innstilltAvEnhet: String? = null,

    @Column(name = "S10_VEDTATT_AV_TYPE", columnDefinition = "CHAR(3)")
    val vedtattAvType: String? = null,

    @Column(name = "S10_VEDTATT_AV_ENHET", columnDefinition = "CHAR(4)")
    val vedtattAvEnhet: String? = null,

    @Column(name = "S10_PRIO_TAB", columnDefinition = "CHAR(6)")
    val prioTab: String? = null,

    @Column(name = "S10_AOE", columnDefinition = "CHAR(3)")
    val aoe: String? = null,

    @Column(name = "S10_ES_SYSTEM", columnDefinition = "CHAR(1)")
    val esSystem: String? = null,

    @Column(name = "S10_ES_GSAK_OPPDRAGSID", columnDefinition = "DECIMAL")
    val esGsakOppdragsid: Long? = null,

    @Column(name = "S10_KNYTTET_TIL_SAK", columnDefinition = "CHAR(2)")
    val knyttetTilSak: String? = null,

    @Column(name = "S10_VEDTAKSTYPE", columnDefinition = "CHAR(1)")
    val vedtakstype: String? = null,

    @Column(name = "S10_REELL_ENHET", columnDefinition = "CHAR(4)")
    val reellEnhet: String? = null,

    @Column(name = "S10_MOD_ENDRET", columnDefinition = "CHAR(1)")
    val modEndret: String? = null,

    @Column(name = "KILDE_IS", columnDefinition = "VARCHAR2")
    val kildeIs: String? = null,

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
