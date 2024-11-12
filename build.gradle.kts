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


// Publishing
jreleaser {
    signing {
        setActive("ALWAYS")
        armored = true
    }
    project {
        authors.set(listOf("UPCI NTUA"))
        license.set("MIT")
        links {
            homepage = "https://api.jaqpot.org"
        }
        description.set("An SDK in Kotlin/Java to access the Jaqpot API (https://api.jaqpot.org)")
        inceptionYear = "2024"
    }

    deploy {
        maven {
            mavenCentral {
                register("app") {
                    setActive("ALWAYS")
                    url.set("https://central.sonatype.com/api/v1/publisher")
                    stagingRepository("target/staging-deploy")
                    username = System.getenv("JRELEASER_MAVENCENTRAL_USERNAME")
                    password = System.getenv("JRELEASER_MAVENCENTRAL_TOKEN")
                }
            }
        }
    }


    distributions {
        register("jaqpot-kotlin-sdk") {
            artifact {
                path.set(file("build/distributions/{{distributionName}}-{{projectVersion}}.zip"))
            }
        }
    }
}


publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "jaqpot-kotlin-sdk"
            from(components["java"])
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
            }
        }
    }
    repositories {
        maven {
            name = "OSSRH"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = System.getenv("JRELEASER_MAVENCENTRAL_USERNAME")
                password = System.getenv("JRELEASER_MAVENCENTRAL_TOKEN")
            }
        }
    }
}

signing {
    val signingKeyId = System.getenv("JRELEASER_GPG_PUBLIC_KEY")
    val signingKey = System.getenv("JRELEASER_GPG_SECRET_KEY")
    val signingPassword = System.getenv("JRELEASER_GPG_PASSPHRASE")
    useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
    sign(publishing.publications["mavenJava"])
}

