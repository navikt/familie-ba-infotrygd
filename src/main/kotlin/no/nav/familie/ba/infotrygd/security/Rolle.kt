package no.nav.familie.ba.infotrygd.security

enum class Rolle {
    FORVALTER,
    APPLICATION,
    ;

    fun authority(): String = "ROLE_$name"
}

