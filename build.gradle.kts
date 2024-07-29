import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val mainClass = "no.nav.familie.ba.infotrygd.Main"


plugins {
    val kotlinVersion = "2.0.0"
    val springBootVersion = "3.3.0"
    id("org.springframework.boot") version springBootVersion
    id("io.spring.dependency-management") version "1.1.6"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    kotlin("plugin.jpa") version kotlinVersion
    id("com.github.ben-manes.versions") version "0.51.0"

    // ------------- SLSA -------------- //
    id("org.cyclonedx.bom") version "1.9.0"
}

configurations {
    implementation.configure {
        exclude(module = "spring-boot-starter-tomcat")
        exclude("org.apache.tomcat")
    }
}

group = "no.nav"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_21

repositories {
    mavenCentral()
    mavenLocal()
    maven {
        name = "Github"
        url = uri("https://maven.pkg.github.com/navikt/nav-foedselsnummer")
        credentials {
            username = "x-access-token" //project.findProperty("gpr.user") as String? ?: System.getenv("GPR_USER")
            password = System.getenv("GPR_API_KEY") ?: project.findProperty("gpr.key") as String?
        }
    }
    maven {
        name = "github-mirror"
        url = uri("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
    }
    maven {
        url = uri("https://maven.pkg.github.com/navikt/maven-release")
        credentials {
            username = System.getenv("GITHUB_USERNAME")
            password = System.getenv("GITHUB_TOKEN")
        }
    }
}

allOpen {
    annotation("javax.persistence.Entity")
    annotation("javax.persistence.MappedSuperclass")
    annotation("javax.persistence.Embeddable")
}

dependencies {

    val mockkVersion = "1.13.12"
    val tokenValidationVersion = "5.0.1"
    val springdocVersion = "2.5.0"
    val navFoedselsnummerVersion = "1.0-SNAPSHOT.6"
    val skattKontraktVersjon = "2.0_20230214104704_706e9c0"
    val fellesVersjon = "3.20240715145751_fc025c1"
    val kontrakterVersjon = "3.0_20231109091547_fd2cae7"
    val coroutinesVersion = "1.8.1"
    val okhttp3Version = "5.0.0-alpha.14"

    // ---------- Spring ---------- \\
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-jetty")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springdoc:springdoc-openapi-starter-common:$springdocVersion")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springdocVersion")

    // ---------- Kotlin ---------- \\
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")

    // ---------- NAV ---------- \\
    implementation("no.nav.familie.kontrakter:felles:$kontrakterVersjon")
    implementation("no.nav.familie.kontrakter:barnetrygd:$kontrakterVersjon")
    implementation("no.nav.familie.eksterne.kontrakter:skatteetaten:$skattKontraktVersjon")
    implementation("no.nav.familie.felles:log:$fellesVersjon")
    implementation("no.nav.familie.felles:leader:$fellesVersjon")
    implementation("no.nav.security:token-validation-spring:$tokenValidationVersion")
    implementation("nav-foedselsnummer:core:$navFoedselsnummerVersion")

    // ---------- DB ---------- \\
    runtimeOnly("org.postgresql:postgresql")
    implementation("com.oracle.database.jdbc:ojdbc8:23.4.0.24.05")

    implementation("com.github.ben-manes.caffeine:caffeine:")
    implementation("io.micrometer:micrometer-core")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("net.ttddyy:datasource-proxy:1.10")
    implementation("javax.inject:javax.inject:1")
    implementation("net.logstash.logback:logstash-logback-encoder:7.4")

    // ---- Test utils ---- \\
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.platform:junit-platform-suite")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:oracle-xe:1.20.0")
    testImplementation("io.mockk:mockk-jvm:$mockkVersion")
    testImplementation("com.h2database:h2")
    testImplementation("com.opencsv:opencsv:5.9")
    testImplementation("nav-foedselsnummer:testutils:$navFoedselsnummerVersion")
    testImplementation("no.nav.security:token-validation-spring-test:$tokenValidationVersion") {
        exclude(group = "com.squareup.okhttp3", module = "mockwebserver")
    }
    testImplementation("com.squareup.okhttp3:mockwebserver:$okhttp3Version")
    testImplementation("com.squareup.okhttp3:okhttp:$okhttp3Version")

}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "21"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.cyclonedxBom {
    setIncludeConfigs(listOf("runtimeClasspath"))
    setSkipConfigs(listOf("compileClasspath", "testCompileClasspath"))
}

extensions.findByName("buildScan")?.withGroovyBuilder {
    setProperty("termsOfServiceUrl", "https://gradle.com/terms-of-service")
    setProperty("termsOfServiceAgree", "yes")
}
