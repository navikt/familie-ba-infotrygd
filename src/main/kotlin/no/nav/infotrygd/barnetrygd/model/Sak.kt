package no.nav.infotrygd.barnetrygd.model

import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.infotrygd.barnetrygd.model.converters.NavLocalDateConverter
import no.nav.infotrygd.barnetrygd.model.converters.ReversedFoedselNrConverter
import java.time.LocalDate
import javax.persistence.*

@Entity
@Table(name = "SA_SAK_10")
data class Sak(
    @Id
    @Column(name = "ID_SAK", columnDefinition = "DECIMAL")
    val idSak: Long,

    @Column(name = "S01_PERSONKEY", columnDefinition = "DECIMAL")
    val s01Personkey: Long? = null,

    @Column(name = "S05_SAKSBLOKK", columnDefinition = "CHAR(1)")
    val s05Saksblokk: String? = null,

    @Column(name = "S10_SAKSNR", columnDefinition = "CHAR(2)")
    val saksnr: String? = null,

    @Column(name = "S10_REG_DATO", columnDefinition = "DECIMAL")
    @Convert(converter = NavLocalDateConverter::class)
    val regDato: LocalDate,

    @Column(name = "S10_MOTTATTDATO", columnDefinition = "DECIMAL")
    @Convert(converter = NavLocalDateConverter::class)
    val mottattdato: LocalDate,

    @Column(name = "S10_KAPITTELNR", columnDefinition = "CHAR(2)")
    val kapittelnr: String? = null,

    @Column(name = "S10_VALG", columnDefinition = "CHAR(2)")
    val valg: String? = null,

    @Column(name = "S10_UNDERVALG_1", columnDefinition = "CHAR(1)")
    val undervalg1: String? = null,

    @Column(name = "S10_DUBLETT_FEIL", columnDefinition = "CHAR(1)")
    val dublettFeil: String? = null,

    @Column(name = "S10_TYPE", columnDefinition = "CHAR(2)")
    val type: String? = null,

    @Column(name = "S10_INNSTILLING", columnDefinition = "CHAR(2)")
    val innstilling: String? = null,

    @Column(name = "S10_RESULTAT", columnDefinition = "CHAR(2)")
    val resultat: String? = null,

    @Column(name = "S10_NIVAA", columnDefinition = "CHAR(3)")
    val nivaa: String? = null,

    @Column(name = "S10_INNSTILLDATO", columnDefinition = "DECIMAL")
    @Convert(converter = NavLocalDateConverter::class)
    val innstilldato: LocalDate? = null,

    @Column(name = "S10_VEDTAKSDATO", columnDefinition = "DECIMAL")
    @Convert(converter = NavLocalDateConverter::class)
    val vedtaksdato: LocalDate? = null,

    @Column(name = "S10_IVERKSATTDATO", columnDefinition = "DECIMAL")
    @Convert(converter = NavLocalDateConverter::class)
    val iverksattdato: LocalDate? = null,

    @Column(name = "S10_GRUNNBL_DATO", columnDefinition = "DECIMAL")
    @Convert(converter = NavLocalDateConverter::class)
    val grunnblDato: LocalDate? = null,

    @Column(name = "S10_AARSAKSKODE", columnDefinition = "CHAR(2)")
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
    @Convert(converter = NavLocalDateConverter::class)
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

    @Column(name = "F_NR",  columnDefinition = "CHAR(11)")
    @Convert(converter = ReversedFoedselNrConverter::class)
    val fNr: FoedselsNr,

    @Column(name = "TK_NR", columnDefinition = "CHAR(4)")
    val tkNr: String? = null,

    @Column(name = "KILDE_IS", columnDefinition = "VARCHAR2")
    val kildeIs: String? = null,

    @Column(name = "REGION", columnDefinition = "CHAR(1)")
    val region: String? = null,
)