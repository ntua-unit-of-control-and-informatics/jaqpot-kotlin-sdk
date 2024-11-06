// build.gradle.kts

plugins {
    kotlin("jvm") version "1.9.20"
    `java-library`
    `maven-publish`
    signing
    id("org.openapi.generator") version "7.9.0"
}

group = "org.jaqpot.kotlinsdk"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.moshi:moshi:1.15.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")

    // coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.9.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
}

tasks.compileKotlin {
    dependsOn(tasks.openApiGenerate)
}

java {
    withJavadocJar()
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

openApiGenerate {
    generatorName.set("kotlin")
    inputSpec.set("$rootDir/../../jaqpot-api/src/main/resources/openapi.yaml")
    outputDir.set("${buildDir}/openapi")
    apiPackage.set("org.jaqpot.kotlinsdk.api")
    modelPackage.set("org.jaqpot.kotlinsdk.model")
    configOptions.set(
        mapOf(
            "dateLibrary" to "java8",
            "useCoroutines" to "false",
            "enumPropertyNaming" to "UPPERCASE",
            "serializationLibrary" to "moshi"
        )
    )
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
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("your-username")
                        name.set("Your Name")
                        email.set("your.email@example.com")
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
                username = System.getenv("OSSRH_USERNAME")
                password = System.getenv("OSSRH_PASSWORD")
            }
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs = listOf("-Xjsr305=strict")
    }
}

sourceSets {
    main {
        kotlin {
            srcDir("${buildDir}/openapi/src/main/kotlin/org")
        }
    }
}
