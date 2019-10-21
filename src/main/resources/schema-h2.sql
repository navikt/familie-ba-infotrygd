CREATE SCHEMA IF NOT EXISTS INFOTRYGD_Q0;

CREATE TABLE ALL_SEQUENCES (
    SEQUENCE_OWNER VARCHAR2(30) NOT NULL  ,
    SEQUENCE_NAME VARCHAR2(30)  NOT NULL  ,
    MIN_VALUE               NUMBER       ,
    MAX_VALUE               NUMBER       ,
    INCREMENT_BY NUMBER  NOT NULL        ,
    CYCLE_FLAG              VARCHAR2(1)  ,
    ORDER_FLAG              VARCHAR2(1)  ,
    CACHE_SIZE   NUMBER  NOT NULL        ,
    LAST_NUMBER  NUMBER  NOT NULL
);

CREATE TABLE ALL_SYNONYMS (
    OWNER              VARCHAR2(30),
    SYNONYM_NAME       VARCHAR2(30),
    TABLE_OWNER        VARCHAR2(30),
    TABLE_NAME         VARCHAR2(30),
    DB_LINK            VARCHAR2(128)
);


--------------------------------------------------
-- Create Table INFOTRYGD_Q0.IS_PERIODE_10
--------------------------------------------------
Create table INFOTRYGD_Q0.IS_PERIODE_10 (
    IS01_PERSONKEY                 NUMBER(15)          DEFAULT 0  NOT NULL,
    IS10_ARBUFOER_SEQ              NUMBER(8,0)           , -- NOT NULL,
    IS10_ARBUFOER                  NUMBER(8)           , -- NOT NULL,
    IS10_ARBKAT                    CHAR(2)             , -- NOT NULL,
    IS10_STAT                      CHAR(1)             , -- NOT NULL,
    IS10_ARBUFOER_TOM              NUMBER(8)           , -- NOT NULL,
    IS10_ARBUFOER_OPPR             NUMBER(8)           , -- NOT NULL,
    IS10_UFOEREGRAD                CHAR(3)             , -- NOT NULL,
    IS10_REVURDERT_DATO            NUMBER(8) ,
    IS10_AARSAK_FORSKYV2           CHAR(2) ,
    IS10_STOENADS_TYPE             CHAR(2) ,
    IS10_REG_DATO_SMII             NUMBER(8) ,
    IS10_ENDR_DATO_STAT            NUMBER(8) ,
    IS10_VENTETID                  CHAR(1) ,
    IS10_INSTOPPH_FOM              CHAR(8) ,
    IS10_INSTOPPH_TOM              CHAR(8) ,
    IS10_BEHDATO                   NUMBER(8) ,
    IS10_KODE_LEGE_INST            CHAR(1) ,
    IS10_LEGENR                    CHAR(11) ,
    IS10_LEGENAVN                  CHAR(25) ,
    IS10_SYKM_II_DATO              NUMBER(8) ,
    IS10_PROGNOSEGRUPPE            CHAR(1) ,
    IS10_UNNTAK                    CHAR(1) ,
    IS10_UKER_VENTETID             CHAR(3) ,
    IS10_BEHANDLING                CHAR(1) ,
    IS10_MELDING_STAT              NUMBER(8) ,
    IS10_SKADEART                  CHAR(1) ,
    IS10_SKDATO                    NUMBER(8) ,
    IS10_SKM_MOTT                  NUMBER(8) ,
    IS10_Y_GODKJ                   CHAR(1) ,
    IS10_FERIE_FOM                 NUMBER(8) ,
    IS10_FERIE_TOM                 NUMBER(8) ,
    IS10_FDATO                     NUMBER(8) ,
    IS10_ANT_BARN                  CHAR(1) ,
    IS10_MORFNR                    NUMBER(11) ,
    IS10_ARBPER                    CHAR(1) ,
    IS10_YRKE                      CHAR(20) ,
    IS10_S_INNT_DATO               NUMBER(8) ,
    IS10_SPATEST                   NUMBER(8) ,
    IS10_STANS                     CHAR(8) ,
    IS10_FRISK                     CHAR(1) ,
    IS10_REGDAT_FRISK              NUMBER(8) ,
    IS10_UTBET_FOM                 NUMBER(8) ,
    IS10_UTBET_TOM                 NUMBER(8) ,
    IS10_S_GRAD                    NUMBER(3) ,
    IS10_FRIB_OPPR_SALD            NUMBER(5) ,
    IS10_FRIB_SALDO                NUMBER(7) ,
    IS10_MAX                       NUMBER(8) ,
    IS10_TIDSYK                    NUMBER(3) ,
    IS10_TIDSYK_KODE               CHAR(1) ,
    IS10_SAKSBLOKK                 CHAR(1) ,
    IS10_GRUPPE                    CHAR(2) ,
    IS10_BRUKERID                  CHAR(7) ,
    IS10_SAK_FRAMLEGG              CHAR(1) ,
    IS10_FERIEDAGER_PERIODE        NUMBER(3) ,
    IS10_FERIEDAGER_PLANLAGT       NUMBER(3) ,
    IS10_STOENAD_OP_PB             CHAR(1) ,
    IS10_STOENAD_OM_SV             CHAR(1) ,
    IS10_INNT_RED_6G               NUMBER(7) ,
    IS10_SAK_TYPE_O_S              CHAR(1) ,
    IS10_SAK_PROGNOSEGRP           CHAR(1) ,
    IS10_DEKNINGSGRAD              NUMBER(3) ,
    IS10_ANT_STOENADSDAGER         NUMBER(3) ,
    IS10_TIDL_UTBET                NUMBER(8) ,
    IS10_TIDL_UTBET_K              CHAR(1) ,
    IS10_PROS_INNTEKT_GR           CHAR(3) ,
    IS10_ANT_BARN_U_12AAR          CHAR(2) ,
    IS10_ALENEOMSORG               CHAR(1) ,
    IS10_ADOPSJONS_DATO            CHAR(8) ,
    IS10_RETT_TIL_FEDREKVOTE       CHAR(1) ,
    IS10_FEDREKVOTE                CHAR(1) ,
    IS10_FEDREKVOTE_TOM            CHAR(8) ,
    IS10_TIDSYK_OP_PB              NUMBER(5) ,
    IS10_EGENOPPL                  CHAR(1) ,
    IS10_ANTATT_SYKM_TOM           CHAR(8) ,
    IS10_VEDTAK_12_UKER            CHAR(8) ,
    IS10_UNNTAK_BS                 CHAR(2) ,
    IS10_REG_DATO                  CHAR(8) ,
    IS10_STILLINGSANDEL_MOR        CHAR(3) ,
    IS10_TIDSK_TYPE                CHAR(2) ,
    IS10_TIDSK_BARNFNR             CHAR(11) ,
    IS10_MAXTIDSK                  CHAR(8) ,
    IS10_SAMMENFALENDE_PERIODE     CHAR(1) ,
    IS10_SAMMENF_DAGER_MASK        CHAR(3) ,
    IS10_DIAGNOSE_KODE_1           CHAR(1) ,
    IS10_DIAGNOSEGRUPPE            CHAR(6) ,
    IS10_DIAGNOSE                  CHAR(70) ,
    IS10_DIAGNOSE_KODE_2           CHAR(1) ,
    IS10_DIAGNOSEGRUPPE_2          CHAR(6) ,
    IS10_DIAGNOSE_2                CHAR(70) ,
    IS10_TERMIN_DATO               CHAR(8) ,
    IS10_KJOEP_HELSETJ             CHAR(2) ,
    IS10_HELSETJ_SENDT             CHAR(8) ,
    IS10_UTREDET_OPERERT           CHAR(1) ,
    IS10_UTREDET_OPERERT_DATO      CHAR(8) ,
    IS10_REG_DATO_HELSETJ          CHAR(8) ,
    IS10_SAMMENHENG_ARB_SIT        CHAR(1) ,
    IS10_ARBEIDSTID_MOR            CHAR(3) ,
    IS10_SITUASJON_MOR             CHAR(1) ,
    IS10_RETTIGHET_MOR             CHAR(1) ,
    IS10_OPPHOLD_FOM               NUMBER(8) ,
    IS10_OPPHOLD_TOM               NUMBER(8) ,
    IS10_DEL2_TYPE                 CHAR(1) ,
    IS10_DEL2_REGDATO_J            NUMBER(8) ,
    IS10_DEL2_REGDATO_U            NUMBER(8) ,
    IS10_DEL2_DATO                 NUMBER(8) ,
    IS10_FRISKM_DATO               NUMBER(8) ,
    IS10_SVANGER_SYK               CHAR(1) ,
    IS10_PAALOGG_ID                CHAR(7) ,
    IS10_UNNTAK_AKTIVITET          CHAR(1) ,
    IS10_OPPFOLGING_DATO           NUMBER(8) ,
    IS10_K68_DATO                  NUMBER(8) ,
    IS10_K69_DATO                  NUMBER(8) ,
    IS10_EOS                       CHAR(1) ,
    IS10_ENGANG                    CHAR(1) ,
    IS10_HALV_HEL                  CHAR(1) ,
    IS10_K28_DATO                  NUMBER(8) ,
    IS10_AARSAK_FORSKYV            CHAR(2) ,
    IS10_STEBARNSADOPSJON          CHAR(1) ,
    IS10_SURROGATMOR               CHAR(1) ,
    IS10_DIALOG1_DATO              NUMBER(8) ,
    IS10_DIALOG1_KODE              CHAR(1) ,
    IS10_DIALOG2_DATO              NUMBER(8) ,
    IS10_DIALOG2_KODE              CHAR(1) ,
    IS10_OPPFOLGING_KODE           CHAR(1) ,
    IS10_K69A_DATO                 NUMBER(8) ,
    IS10_POLIKL_BEH                CHAR(8) ,
    IS10_ARENA_F234                CHAR(1) ,
    IS10_AVVENT_SYKMELD            CHAR(1) ,
    IS10_AVVENT_TOM                NUMBER(8) ,
    IS10_ARENA_F226                CHAR(1) ,
    IS10_ARBKAT_99                 CHAR(2) ,
    IS10_PB_BEKREFT                CHAR(1) ,
    IS10_SANKSJON_FOM              NUMBER(8) ,
    IS10_SANKSJON_TOM              NUMBER(8) ,
    IS10_SANKSJONSDAGER            NUMBER(3) ,
    IS10_SANKSJON_BEKREFTET        CHAR(1) ,
    IS10_RETT_TIL_MODREKVOTE       CHAR(1) ,
    IS10_MODREKVOTE                CHAR(1) ,
    IS10_MODREKVOTE_TOM            CHAR(8) ,
    IS10_FERIE_FOM2                NUMBER(8) ,
    IS10_FERIE_TOM2                NUMBER(8) ,
    IS10_STOPPDATO                 NUMBER(8) ,
    TK_NR                          CHAR(4)             , -- NOT NULL,
    F_NR                           CHAR(11)            , -- NOT NULL,
    OPPRETTET                      TIMESTAMP(6)        DEFAULT localtimestamp  , -- NOT NULL,
    ENDRET_I_KILDE                 TIMESTAMP(6)        DEFAULT localtimestamp  , -- NOT NULL,
    KILDE_IS                       VARCHAR2(12)        DEFAULT ' '  , -- NOT NULL,
    REGION                         CHAR(1)             DEFAULT ' '  , -- NOT NULL,
    ID_PERI10                      NUMBER              DEFAULT NOT NULL); -- endret fra NUMBER

--------------------------------------------------
-- Create Table INFOTRYGD_Q0.IS_UTBETALING_15
--------------------------------------------------
Create table INFOTRYGD_Q0.IS_UTBETALING_15 (
    IS01_PERSONKEY                 NUMBER(15)          , -- NOT NULL,
    IS10_ARBUFOER_SEQ              NUMBER(8)           , -- NOT NULL,
    IS15_UTBETFOM_SEQ              NUMBER(8)           , -- NOT NULL,
    IS15_UTBETFOM                  NUMBER(8)           , -- NOT NULL,
    IS15_UTBETTOM                  NUMBER(8)           , -- NOT NULL,
    IS15_UTBETDATO                 NUMBER(8)           , -- NOT NULL,
    IS15_ARBGIVNR                  NUMBER(11)          , -- NOT NULL,
    IS15_BILAG                     NUMBER(7)           , -- NOT NULL,
    IS15_DSATS                     NUMBER(9, 2)        , -- NOT NULL,
    IS15_GRAD                      CHAR(3)             , -- NOT NULL,
    IS15_OP                        CHAR(2)             , -- NOT NULL,
    IS15_TYPE                      CHAR(1)             , -- NOT NULL,
    IS15_TILB_UTBETDATO            NUMBER(8)           , -- NOT NULL,
    IS15_TILB_BILAG                NUMBER(7)           , -- NOT NULL,
    IS15_TILB_OP                   CHAR(2)             , -- NOT NULL,
    IS15_TIDSKONTO_KODE            CHAR(1)             , -- NOT NULL,
    IS15_BRUKERID                  CHAR(7)             , -- NOT NULL,
    IS15_REGDATO_BATCH             NUMBER(8)           , -- NOT NULL,
    IS15_TILTAK_TYPE               CHAR(2)             , -- NOT NULL,
    IS15_KORR                      CHAR(4)             , -- NOT NULL,
    IS15_AARSAK_FORSKYV            CHAR(2)             , -- NOT NULL,
    IS15_BEREGNET_I_OS             CHAR(1)             , -- NOT NULL,
    TK_NR                          CHAR(4)             , -- NOT NULL,
    F_NR                           CHAR(11)            , -- NOT NULL,
    OPPRETTET                      TIMESTAMP(6)        DEFAULT current_timestamp  , -- NOT NULL,
    ENDRET_I_KILDE                 TIMESTAMP(6)        DEFAULT current_timestamp  , -- NOT NULL,
    KILDE_IS                       VARCHAR2(12)        DEFAULT ' '  , -- NOT NULL,
    REGION                         CHAR(1)             DEFAULT ' '  , -- NOT NULL,
    ID_UTBT                        NUMBER              DEFAULT NOT NULL);

--------------------------------------------------
-- Create Table INFOTRYGD_Q0.IS_INNTEKT_13
--------------------------------------------------
Create table INFOTRYGD_Q0.IS_INNTEKT_13 (
    IS01_PERSONKEY                 NUMBER(15)          , -- NOT NULL,
    IS10_ARBUFOER_SEQ              NUMBER(8)           , -- NOT NULL,
    IS13_SPFOM                     NUMBER(8)           , -- NOT NULL,
    IS13_ARBGIVNR                  NUMBER(11)          , -- NOT NULL,
    IS13_LOENN                     NUMBER(11, 2)       , -- NOT NULL,
    IS13_PERIODE                   CHAR(1)             , -- NOT NULL,
    IS13_REF                       CHAR(1)             , -- NOT NULL,
    IS13_REF_TOM                   NUMBER(8)           , -- NOT NULL,
    IS13_GML_SPFOM                 NUMBER(8)           , -- NOT NULL,
    IS13_GML_LOENN                 NUMBER(11, 2)       , -- NOT NULL,
    IS13_GML_PERIODE               CHAR(1)             , -- NOT NULL,
    IS13_PO_INNT                   CHAR(1)             , -- NOT NULL,
    IS13_UTBET                     CHAR(1)             , -- NOT NULL,
    IS13_TIDSKONTO_KODE            CHAR(1)             , -- NOT NULL,
    TK_NR                          CHAR(4)             , -- NOT NULL,
    F_NR                           CHAR(11)            , -- NOT NULL,
    OPPRETTET                      TIMESTAMP(6)        DEFAULT current_timestamp  , -- NOT NULL,
    ENDRET_I_KILDE                 TIMESTAMP(6)        DEFAULT current_timestamp  , -- NOT NULL,
    KILDE_IS                       VARCHAR2(12)        DEFAULT ' '  , -- NOT NULL,
    REGION                         CHAR(1)             DEFAULT ' '  , -- NOT NULL,
    ID_INNT                        NUMBER              DEFAULT NOT NULL);

--------------------------------------------------
-- Create Table INFOTRYGD_Q0.T_LOPENR_FNR
--------------------------------------------------
Create table INFOTRYGD_Q0.T_LOPENR_FNR (
    PERSON_LOPENR                  NUMBER              NOT NULL,
    PERSONNR                       CHAR(11)            NOT NULL,
    OPPRETTET                      TIMESTAMP(6)        DEFAULT current_timestamp NOT NULL);

--------------------------------------------------
-- Create Table INFOTRYGD_Q0.T_STONAD
--------------------------------------------------
Create table INFOTRYGD_Q0.T_STONAD (
    STONAD_ID                      NUMBER              NOT NULL,
    PERSON_LOPENR                  NUMBER              , -- NOT NULL,
    KODE_RUTINE                    CHAR(2)             , -- NOT NULL,
    DATO_START                     DATE                , -- NOT NULL,
    KODE_OPPHOR                    CHAR(2) ,
    DATO_OPPHOR                    DATE ,
    OPPDRAG_ID                     NUMBER ,
    TIDSPUNKT_OPPHORT              TIMESTAMP(6) ,
    TIDSPUNKT_REG                  TIMESTAMP(6) ,
    BRUKERID                       CHAR(8) ,
    OPPRETTET                      TIMESTAMP(6)        DEFAULT current_timestamp NOT NULL);

--------------------------------------------------
-- Create Table INFOTRYGD_Q0.T_VEDTAK
--------------------------------------------------
Create table INFOTRYGD_Q0.T_VEDTAK (
    VEDTAK_ID                      NUMBER              NOT NULL,
    PERSON_LOPENR                  NUMBER              NOT NULL,
    KODE_RUTINE                    CHAR(2)             , -- NOT NULL,
    DATO_START                     DATE                , -- NOT NULL,
    TKNR                           CHAR(4)             , -- NOT NULL,
    SAKSBLOKK                      CHAR(1)             , -- NOT NULL,
    SAKSNR                         NUMBER              , -- NOT NULL,
    TYPE_SAK                       CHAR(2)             , -- NOT NULL,
    KODE_RESULTAT                  CHAR(2)             , -- NOT NULL,
    DATO_INNV_FOM                  DATE                , -- NOT NULL,
    DATO_INNV_TOM                  DATE ,
    DATO_MOTTATT_SAK               DATE                , -- NOT NULL,
    KODE_VEDTAKSNIVAA              CHAR(3)             , -- NOT NULL,
    TYPE_BEREGNING                 CHAR(3)             , -- NOT NULL,
    TKNR_BEH                       CHAR(4)             , -- NOT NULL,
    TIDSPUNKT_REG                  TIMESTAMP(6)        , -- NOT NULL,
    BRUKERID                       CHAR(8)             , -- NOT NULL,
    NOKKEL_DL1                     CHAR(30) ,
    ALTERNATIV_MOTTAKER            NUMBER(11) ,
    STONAD_ID                      NUMBER              , -- NOT NULL,
    KIDNR                          VARCHAR2(25) ,
    FAKTNR                         VARCHAR2(33) ,
    OPPRETTET                      TIMESTAMP(6)        DEFAULT current_timestamp NOT NULL);

--------------------------------------------------
-- Create Table INFOTRYGD_Q0.T_VEDTAK_SP_FA_BS
--------------------------------------------------
Create table INFOTRYGD_Q0.T_VEDTAK_SP_FA_BS (
    VEDTAK_ID                      NUMBER              NOT NULL,
    ARBKAT                         CHAR(2)             NOT NULL,
    KODE_FORELDREKVOTE             CHAR(1)             , -- NOT NULL,
    DEKNINGSGRAD                   NUMBER              , -- NOT NULL,
    DATO_FODSEL                    DATE ,
    DATO_ADOPSJON                  DATE ,
    ANT_BARN                       NUMBER ,
    ORGNR_JURIDISK                 CHAR(9) ,
    DATO_OPPHOR_FOM                DATE ,
    PLEIEPENGEGRAD                 NUMBER ,
    BRUKERID                       CHAR(8)             , --NOT NULL,
    TIDSPUNKT_REG                  TIMESTAMP(6)        , -- NOT NULL,
    OPPRETTET                      TIMESTAMP(6)        DEFAULT current_timestamp  NOT NULL);

--------------------------------------------------
-- Create Table INFOTRYGD_Q0.T_DELYTELSE
--------------------------------------------------
Create table INFOTRYGD_Q0.T_DELYTELSE (
    VEDTAK_ID                      NUMBER              NOT NULL,
    TYPE_DELYTELSE                 CHAR(2)             NOT NULL,
    TIDSPUNKT_REG                  TIMESTAMP(6)        NOT NULL,
    FOM                            DATE                , -- NOT NULL,
    TOM                            DATE ,
    BELOP                          NUMBER(11, 2)       , -- NOT NULL,
    OPPGJORSORDNING                CHAR(1) ,
    MOTTAKER_LOPENR                NUMBER ,
    BRUKERID                       CHAR(8)             , -- NOT NULL,
    TYPE_SATS                      CHAR(1)             , -- NOT NULL,
    TYPE_UTBETALING                CHAR(1)             , -- NOT NULL,
    LINJE_ID                       NUMBER ,
    OPPRETTET                      TIMESTAMP(6)        DEFAULT current_timestamp NOT NULL);

--------------------------------------------------
-- Create Table INFOTRYGD_Q0.T_DELYTELSE_SP_FA_BS
--------------------------------------------------
Create table INFOTRYGD_Q0.T_DELYTELSE_SP_FA_BS (
    VEDTAK_ID                      NUMBER              NOT NULL,
    TYPE_DELYTELSE                 CHAR(2)             NOT NULL,
    TIDSPUNKT_REG                  TIMESTAMP(6)        NOT NULL,
    TYPE_INNTEKT                   CHAR(2)             , -- NOT NULL,
    TYPE_TILTAK                    CHAR(2)             , -- NOT NULL,
    TYPE_FORSIKRING                CHAR(1)             , -- NOT NULL,
    PERIODE_KARENS                 CHAR(1)             , -- NOT NULL,
    PROSENT_INNT_GRL               NUMBER              , -- NOT NULL,
    ORGNR                          CHAR(9)             , -- NOT NULL,
    REFUSJON                       CHAR(1)             , -- NOT NULL,
    GRAD                           NUMBER              , -- NOT NULL,
    DATO_MAX                       DATE ,
    KODE_KLASSE                    CHAR(20)            , -- NOT NULL,
    SATSENDRING                    CHAR(1)             , -- NOT NULL,
    DATO_ANNULLERT                 DATE ,
    SJOMANN                        CHAR(1)             , -- NOT NULL,
    TYPE_SATS                      CHAR(4)             , -- NOT NULL,
    SATS_DAGER                     NUMBER(7, 2)        , -- NOT NULL,
    BRUKERID                       CHAR(8)             , -- NOT NULL,
    OPPRETTET                      TIMESTAMP(6)        DEFAULT current_timestamp NOT NULL);

--------------------------------------------------
-- Create Table INFOTRYGD_Q0.T_STONAD_BS
--------------------------------------------------
Create table INFOTRYGD_Q0.T_STONAD_BS (
    STONAD_ID                      NUMBER              NOT NULL,
    UNNTAK                         CHAR(2)             , -- NOT NULL,
    PLEIEPENGEGRAD                 NUMBER ,
    LOPENR_BARN                    NUMBER ,
    BRUKERID                       CHAR(8)             , -- NOT NULL,
    TIDSPUNKT_REG                  TIMESTAMP(6)        , -- NOT NULL,
    OPPRETTET                      TIMESTAMP(6)        DEFAULT current_timestamp  NOT NULL);

--------------------------------------------------
-- Create Table INFOTRYGD_Q0.T_INNTEKT
--------------------------------------------------
Create table INFOTRYGD_Q0.T_INNTEKT (
    STONAD_ID                      NUMBER              NOT NULL,
    ORGNR                          NUMBER              NOT NULL,
    INNTEKT_FOM                    DATE                NOT NULL,
    LOPENR                         NUMBER              NOT NULL,
    INNTEKT_TOM                    DATE ,
    TYPE_INNTEKT                   CHAR(1)             NOT NULL,
    INNTEKT                        NUMBER(13, 2)       NOT NULL,
    PERIODE                        CHAR(1)             NOT NULL,
    REFUSJON                       CHAR(1)             NOT NULL,
    REFUSJON_TOM                   DATE ,
    STATUS                         CHAR(1)             NOT NULL,
    BRUKERID                       CHAR(8)             NOT NULL,
    TIDSPUNKT_REG                  TIMESTAMP(6)        NOT NULL);
