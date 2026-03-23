package no.nav.familie.ba.infotrygd.security

enum class Rolle {
    FORVALTER,
    APPLICATION,
    SAKSBEHANDLER,
    ;

    fun authority(): String = "ROLE_$name"
}

