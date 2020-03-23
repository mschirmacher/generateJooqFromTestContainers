import nu.studer.gradle.jooq.JooqTask
import org.gradle.api.JavaVersion.VERSION_1_8

buildscript {
    repositories {
        mavenCentral()
    }
}

repositories {
    mavenCentral()
}

plugins {
    id("java")
    id("org.flywaydb.flyway") version "6.3.1"
    id("nu.studer.jooq")
}

group = "com.example"
version = "1.0-SNAPSHOT"

val jooqVersion = "3.13.1"
val testcontainersVersion = "1.12.5"
val flywayVersion = "6.3.1"
val postgresqlVersion = "42.2.11"

java.sourceCompatibility = VERSION_1_8

val flywayMigration by configurations.creating

dependencies {
//    implementation("org.flywaydb:flyway-core:$flywayVersion")
//    implementation("org.jooq:jooq:$jooqVersion")

//    compileClasspath("org.testcontainers:postgresql:$testcontainersVersion")
//    compileClasspath("org.postgresql:postgresql:$postgresqlVersion")
//    jooqRuntime("org.postgresql:postgresql:$postgresqlVersion")

    flywayMigration("org.testcontainers:postgresql:$testcontainersVersion")
    flywayMigration("org.postgresql:postgresql:$postgresqlVersion")
}

val jdbcUrl = "jdbc:tc:postgresql:10.5:///generationDb"
val dbUser = "user"
val dbPassword = "password"

flyway {
    url = jdbcUrl
    user = dbUser
    password = dbPassword
    locations = arrayOf("filesystem:${projectDir}/src/main/resources/db/migration")

    configurations = arrayOf(flywayMigration.name)
}

val jooqConfigName = "generationSchema"

jooq {
    version = jooqVersion
    edition = nu.studer.gradle.jooq.JooqEdition.OSS
    generateSchemaSourceOnCompilation = false

    jooqConfigName(sourceSets["main"]) {
        jdbc {
            url = jdbcUrl
            user = dbUser
            password = dbPassword
            schema = "public"
        }
        generator {
            name = "org.jooq.codegen.DefaultGenerator"
            database {
                includes = ".*"
                excludes = ""
            }
            generate {
                isDeprecated = false
                isRecords = false
                isImmutablePojos = false
                isFluentSetters = false
                isGeneratedAnnotation = false
            }
            target {
                packageName = "com.example"
                directory = "${projectDir}/src/main/java/"
            }
            strategy {
                name = "org.jooq.codegen.DefaultGeneratorStrategy"
            }
        }
    }
}

val flywayTask = tasks.getByName("flywayMigrate")

tasks.withType<JooqTask> {
    dependsOn.add(flywayTask)
}
