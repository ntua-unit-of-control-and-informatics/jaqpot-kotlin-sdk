// build.gradle.kts

plugins {
    kotlin("jvm") version "1.9.20"
    `java-library`
    application
    `maven-publish`
    signing
    id("org.openapi.generator") version "7.9.0"
    id("org.jreleaser") version "1.15.0"
}

group = "org.jaqpot.kotlinsdk"
version = "0.3.0"

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

tasks.compileKotlin {
    dependsOn(tasks.openApiGenerate)
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

java {
    withJavadocJar()
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

openApiGenerate {
    generatorName.set("java")
    remoteInputSpec.set("https://raw.githubusercontent.com/ntua-unit-of-control-and-informatics/jaqpot-api/refs/heads/main/src/main/resources/openapi.yaml")
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


tasks.getByName("openApiGenerate").dependsOn(tasks.named<Jar>("sourcesJar"))


jreleaser {
    signing {
        setActive("ALWAYS")
        armored = true
    }

    checksum {
        // The name of the grouping checksums file.
        // Defaults to `checksums.txt`.
        //
        name.set("{{projectName}}-{{projectVersion}}_checksums.txt")

        // Uploads individual checksum files.
        // Defaults to `false`.
        //
        individual = true

        // Whether to checksum artifacts in the `distributions` section or not.
        // Defaults to `true`.
        //
        artifacts = true

        // Whether to checksum files in the `files` section or not.
        // Defaults to `true`.
        //
        files = true
    }

    deploy {
        maven {
            mavenCentral {
                register("sonatype") {
                    setActive("ALWAYS")
                    url.set("https://central.sonatype.com/api/v1/publisher")
                    stagingRepository("${layout.buildDirectory.get()}/staging-deploy")
                    username = System.getenv("SONATYPE_USERNAME")
                    password = System.getenv("SONATYPE_PASSWORD")
                    applyMavenCentralRules = true
                    verifyPom = true
                }
            }
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "kotlin-sdk"
            from(components["java"])
            pom {
                name.set("Jaqpot Kotlin SDK")
                description.set("Java/Kotlin SDK for the Jaqpot API")
                url.set("https://github.com/ntua-unit-of-control-and-informatics/kotlin-sdk")
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
            url = layout.buildDirectory.dir("staging-deploy").get().asFile.toURI()
        }
    }
}

signing {
    val signingKey = System.getenv("JRELEASER_GPG_SECRET_KEY")
    val signingPassword = System.getenv("JRELEASER_GPG_PASSPHRASE")
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications["mavenJava"])
}

