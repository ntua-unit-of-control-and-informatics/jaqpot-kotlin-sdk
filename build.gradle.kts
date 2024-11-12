// build.gradle.kts

import java.net.URI

plugins {
    kotlin("jvm") version "1.9.20"
    `java-library`
    `maven-publish`
    signing
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
    id("org.openapi.generator") version "7.9.0"
}

group = "org.jaqpot.kotlin-sdk"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.11.0")

    implementation("jakarta.validation:jakarta.validation-api:3.1.0")
    implementation("jakarta.annotation:jakarta.annotation-api:3.0.0")

    implementation("io.gsonfire:gson-fire:1.9.0")
    implementation("org.apache.oltu.oauth2:org.apache.oltu.oauth2.client:1.0.1")
    implementation("org.json:json:20231013")

    implementation("com.squareup.okio:okio:3.4.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
}




tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs = listOf("-Xjsr305=strict")
    }
}

sourceSets {
    main {
        java {
            srcDir("${layout.buildDirectory.get()}/src/main/java")
        }
    }
}

tasks.create<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
    dependsOn("openApiGenerate")
}

tasks.compileKotlin {
    dependsOn(tasks.openApiGenerate)
}

tasks.openApiGenerate {
    mustRunAfter("downloadOpenApiSpec")
}

java {
    withJavadocJar()
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.register("downloadOpenApiSpec") {
    doLast {
        // Create temporary directory if it doesn't exist
        val tmpDir = file("${layout.buildDirectory.get()}/tmp")
        tmpDir.mkdirs()

        // Download and process the file
        val tempFile = file("${layout.buildDirectory.get()}/tmp/openapi.yaml")
        val url =
            URI("https://raw.githubusercontent.com/ntua-unit-of-control-and-informatics/jaqpot-api/refs/heads/main/src/main/resources/openapi.yaml")
                .toURL()

        // Download and filter in one go
        tempFile.writeText(
            url.openStream().bufferedReader().useLines { lines ->
                lines.filter { !it.contains("x-field-extra-annotation:") }
                    .joinToString("\n")
            }
        )
    }
}

openApiGenerate {
    generatorName.set("java")
    inputSpec.set("${layout.buildDirectory.get()}/tmp/openapi.yaml")
    outputDir.set("${layout.buildDirectory.get()}")
    configOptions.set(
        mapOf(
            "generateBuilders" to "true",
            "library" to "retrofit2",
            "hideGenerationTimestamp" to "true",
            "useJakartaEe" to "true",
            "openApiNullable" to "false"
        )
    )
}

// Configures publishing to Maven Central
nexusPublishing {
    repositories {
        sonatype {  // only for users registered in Sonatype after 24 Feb 2021
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            username.set(System.getenv("SONATYPE_USERNAME") ?: properties["sonatypeUsername"].toString())
            password.set(System.getenv("SONATYPE_PASSWORD") ?: properties["sonatypePassword"].toString())
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = "org.jaqpot"
            artifactId = "kotlin-sdk"
            version = "0.1.0"
            pom {
                name.set("Jaqpot Kotlin SDK")
                description.set("Java/Kotlin SDK for the Jaqpot API")
                url.set("https://github.com/ntua-unit-of-control-and-informatics/jaqpot-kotlin-sdk")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        id.set("upci")
                        name.set("Alex Arvanitidis")
                        email.set("upci.ntua@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/ntua-unit-of-control-and-informatics/jaqpot-kotlin-sdk.git")
                    developerConnection.set("scm:git:ssh://github.com:ntua-unit-of-control-and-informatics/jaqpot-kotlin-sdk.git")
                    url.set("https://github.com/ntua-unit-of-control-and-informatics/jaqpot-kotlin-sdk")
                }
            }
        }
    }
    repositories {
        maven {
            name = "OSSRH"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = System.getenv("OSSRH_USERNAME")
                password = System.getenv("OSSRH_PASSWORD")
            }
        }
    }
}

signing {
    val signingKey = System.getenv("GPG_SIGNING_KEY") ?: properties["signing.key"].toString()
    val signingPassword = System.getenv("GPG_SIGNING_PASSWORD") ?: properties["signing.password"].toString()
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications["mavenJava"])
}

