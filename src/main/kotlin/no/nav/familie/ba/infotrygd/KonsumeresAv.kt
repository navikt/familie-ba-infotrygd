package no.nav.familie.ba.infotrygd

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class KonsumeresAv(vararg val apper: String)
