package no.nav.infotrygd.barnetrygd.rest.api

data class InfotrygdSøkResponse(val ingenTreff: Boolean)

data class InfotrygdLøpendeBarnetrygdResponse(val harLøpendeBarnetrygd: Boolean)

data class InfotrygdÅpenSakResponse(val harÅpenSak: Boolean)