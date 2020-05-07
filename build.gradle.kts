import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val logbackVersion = "1.2.3"
val logstashVersion = "5.3"
val junitJupiterVersion = "5.4.2"
val mockkVersion = "1.9.3"
val wireMockVersion = "2.19.0"
val filformatVersion = "1.2019.06.26-14.50-746e7610cb12"
val micrometerRegistryVersion = "1.1.2"
val tokenSupportVersion = "0.2.18"
val jacksonVersion = "2.9.9"
val swaggerVersion = "2.9.2"
val oracleusername = "richard.martinsen@nav.no"
val oraclepassword = "Infotrygd1"
val navFoedselsnummerVersion = "1.0-SNAPSHOT.5"

val mainClass = "no.nav.infotrygd.kontantstotte.InfotrygdBeregningsgrunnlagApplication"


plugins {
    val kotlinVersion = "1.3.31"
    val springBootVersion = "2.1.6.RELEASE"
    id("org.springframework.boot") version springBootVersion
    id("io.spring.dependency-management") version "1.0.7.RELEASE"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    kotlin("plugin.jpa") version kotlinVersion
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
    implementation("io.micrometer:micrometer-core")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("no.nav.security:oidc-spring-support:0.2.18")
    implementation("net.ttddyy:datasource-proxy:1.4.1")
    testImplementation("no.nav.security:oidc-test-support:0.2.18")
    implementation("javax.inject:javax.inject:1")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.springfox:springfox-swagger2:$swaggerVersion")
    implementation("io.springfox:springfox-swagger-ui:$swaggerVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:5.1")
    compile("org.springframework.boot:spring-boot-starter-jdbc")
    compile("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("org.postgresql:postgresql")
    implementation("com.oracle.ojdbc:ojdbc8:19.3.0.0")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:oracle-xe:1.12.1")
    compile("com.h2database:h2")
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