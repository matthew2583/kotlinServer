plugins {
    kotlin("jvm") version "2.2.10"
    application
    id("org.jlleitschuh.gradle.ktlint") version "13.1.0"
}

group = "ru.yarsu"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("ru.yarsu.MainKt")
}

repositories {
    mavenCentral()
}

val jacksonVersion = "2.18.2"
val http4kVersion = "6.5.0.0"
val junitVersion = "5.11.4"

dependencies {
    // HTTP4K Web Framework
    implementation("org.http4k:http4k-core:$http4kVersion")
    implementation("org.http4k:http4k-server-netty:$http4kVersion")
    implementation("org.http4k:http4k-format-jackson:$http4kVersion")

    // Jackson for JSON
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")

    // JWT Authentication
    implementation("com.auth0:java-jwt:4.5.0")

    // CSV Processing
    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.10.0")

    // CLI Arguments
    implementation("com.beust:jcommander:1.82")

    // Test dependencies
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    testImplementation("io.mockk:mockk:1.13.16")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
    }
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "ru.yarsu.MainKt"
    }
}

val ktlintVersion: String by project

ktlint {
    version.set(ktlintVersion)
}
