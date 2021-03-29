import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.ServiceFileTransformer
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "no.nav.syfo"
version = "1.0.0"

object Versions {
    const val arrowVersion = "0.9.0"
    const val coroutinesVersion = "1.4.2"
    const val flywayVersion = "7.5.0"
    const val hikariVersion = "3.4.5"
    const val jacksonVersion = "2.11.3"
    const val kafkaVersion = "2.3.1"
    const val kafkaEmbeddedVersion = "2.4.0"
    const val kluentVersion = "1.61"
    const val ktorVersion = "1.5.0"
    const val logbackVersion = "1.2.3"
    const val logstashEncoderVersion = "6.3"
    const val mockkVersion = "1.10.5"
    const val nimbusjosejwtVersion = "7.5.1"
    const val postgresVersion = "42.2.18"
    const val postgresTestContainersVersion = "1.15.1"
    const val prometheusVersion = "0.9.0"
    const val spekVersion = "2.0.15"
    const val vaultJavaDriveVersion = "3.1.0"
}

plugins {
    kotlin("jvm") version "1.4.21"
    id("com.github.johnrengelman.shadow") version "6.1.0"
    id("org.jlleitschuh.gradle.ktlint") version "9.4.1"
}

repositories {
    mavenCentral()
    jcenter()
    maven(url = "https://packages.confluent.io/maven/")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))

    implementation("io.ktor:ktor-server-netty:${Versions.ktorVersion}")
    implementation("io.ktor:ktor-auth:${Versions.ktorVersion}")
    implementation("io.ktor:ktor-auth-jwt:${Versions.ktorVersion}")
    implementation("io.ktor:ktor-jackson:${Versions.ktorVersion}")
    implementation("io.ktor:ktor-client-apache:${Versions.ktorVersion}")
    implementation("io.ktor:ktor-client-cio:${Versions.ktorVersion}")
    implementation("io.ktor:ktor-client-jackson:${Versions.ktorVersion}")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j:${Versions.coroutinesVersion}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutinesVersion}")

    implementation("io.prometheus:simpleclient_hotspot:${Versions.prometheusVersion}")
    implementation("io.prometheus:simpleclient_common:${Versions.prometheusVersion}")

    implementation("ch.qos.logback:logback-classic:${Versions.logbackVersion}")
    implementation("net.logstash.logback:logstash-logback-encoder:${Versions.logstashEncoderVersion}")

    implementation("org.postgresql:postgresql:${Versions.postgresVersion}")
    implementation("com.zaxxer:HikariCP:${Versions.hikariVersion}")
    implementation("org.flywaydb:flyway-core:${Versions.flywayVersion}")
    implementation("com.bettercloud:vault-java-driver:${Versions.vaultJavaDriveVersion}")
    testImplementation("org.testcontainers:postgresql:${Versions.postgresTestContainersVersion}")

    implementation("org.apache.kafka:kafka_2.12:${Versions.kafkaVersion}")
    testImplementation("no.nav:kafka-embedded-env:${Versions.kafkaEmbeddedVersion}")

    implementation("com.fasterxml.jackson.core:jackson-databind:${Versions.jacksonVersion}")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${Versions.jacksonVersion}")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${Versions.jacksonVersion}")

    implementation("io.arrow-kt:arrow-core-data:${Versions.arrowVersion}")

    testImplementation("com.nimbusds:nimbus-jose-jwt:${Versions.nimbusjosejwtVersion}")
    testImplementation("org.amshove.kluent:kluent:${Versions.kluentVersion}")
    testImplementation("io.ktor:ktor-server-test-host:${Versions.ktorVersion}")
    testImplementation("io.mockk:mockk:${Versions.mockkVersion}")
    testImplementation("org.spekframework.spek2:spek-dsl-jvm:${Versions.spekVersion}")
    testRuntimeOnly("org.spekframework.spek2:spek-runtime-jvm:${Versions.spekVersion}")
    testRuntimeOnly("org.spekframework.spek2:spek-runner-junit5:${Versions.spekVersion}")
}

tasks {
    withType<Jar> {
        manifest.attributes["Main-Class"] = "no.nav.syfo.BootstrapKt"
    }

    create("printVersion") {
        doLast {
            println(project.version)
        }
    }

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }

    withType<ShadowJar> {
        transform(ServiceFileTransformer::class.java) {
            setPath("META-INF/cxf")
            include("bus-extensions.txt")
        }
    }

    withType<Test> {
        useJUnitPlatform {
            includeEngines("spek2")
        }
        testLogging.showStandardStreams = true
    }
}
