import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val logbackVersion = "1.2.3"
val logstashVersion = "5.3"
val junitJupiterVersion = "5.4.2"
val mockkVersion = "1.12.1"
val wireMockVersion = "2.19.0"
val filformatVersion = "1.2019.06.26-14.50-746e7610cb12"
val micrometerRegistryVersion = "1.1.2"
val tokenSupportVersion = "1.3.9"
val jacksonVersion = "2.9.9"
val swaggerVersion = "3.0.0"
val oracleusername = "richard.martinsen@nav.no"
val oraclepassword = "Infotrygd1"
val navFoedselsnummerVersion = "1.0-SNAPSHOT.6"
val skattKontraktVersjon = "2.0_20210920094114_9c74239"
val fellesVersjon = "1.20211217154554_3bb883e"
val kontrakterVersjon = "2.0_20220105124543_5666f57"

val mainClass = "no.nav.familie.ba.infotrygd.Main"


plugins {
    val kotlinVersion = "1.5.31"
    val springBootVersion = "2.5.5"
    id("org.springframework.boot") version springBootVersion
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    kotlin("plugin.jpa") version kotlinVersion
    id("com.github.ben-manes.versions") version "0.39.0"
}

group = "no.nav"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
    maven {
        name = "Github"
        url = uri("https://maven.pkg.github.com/navikt/nav-foedselsnummer")
        credentials {
            username = "x-access-token" //project.findProperty("gpr.user") as String? ?: System.getenv("GPR_USER")
            password = System.getenv("GPR_API_KEY") ?: project.findProperty("gpr.key") as String?
        }
    }
}

allOpen {
    annotation("javax.persistence.Entity")
    annotation("javax.persistence.MappedSuperclass")
    annotation("javax.persistence.Embeddable")
}

dependencies {
    implementation("nav-foedselsnummer:core:$navFoedselsnummerVersion")
    testImplementation("nav-foedselsnummer:testutils:$navFoedselsnummerVersion")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("com.github.ben-manes.caffeine:caffeine:")
    implementation("io.micrometer:micrometer-core")
    implementation("no.nav.familie.kontrakter:felles:$kontrakterVersjon")
    implementation("no.nav.familie.kontrakter:barnetrygd:$kontrakterVersjon")
    implementation("no.nav.familie.felles:log:$fellesVersjon")
    implementation("no.nav.familie.felles:leader:$fellesVersjon")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("net.ttddyy:datasource-proxy:1.7")
    implementation("no.nav.security:token-validation-spring:$tokenSupportVersion")
    testImplementation("no.nav.security:token-validation-test-support:$tokenSupportVersion")
    implementation("javax.inject:javax.inject:1")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.springfox:springfox-boot-starter:$swaggerVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:6.6")
    implementation("no.nav.familie.eksterne.kontrakter:skatteetaten:$skattKontraktVersjon")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("org.postgresql:postgresql")
    implementation("com.oracle.database.jdbc:ojdbc8:19.12.0.0")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:oracle-xe:1.16.2")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("com.h2database:h2")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}

extensions.findByName("buildScan")?.withGroovyBuilder {
    setProperty("termsOfServiceUrl", "https://gradle.com/terms-of-service")
    setProperty("termsOfServiceAgree", "yes")
}
