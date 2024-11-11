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

tasks.register<Copy>("filterOpenApiSpec") {
    from("$rootDir/../jaqpot-api/src/main/resources/openapi.yaml")
    into("$buildDir/tmp")
    filter { line ->
        // Remove the x-field-extra-annotation line
        if (line.contains("x-field-extra-annotation:")) {
            ""
        } else {
            line
        }
    }
}

tasks.compileKotlin {
    dependsOn(tasks.openApiGenerate)
}

tasks.openApiGenerate {
    mustRunAfter("filterOpenApiSpec")
}

java {
    withJavadocJar()
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}


openApiGenerate {
    generatorName.set("java")
    inputSpec.set("$buildDir/tmp/openapi.yaml")
    outputDir.set("${buildDir}")
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
        java {
            srcDir("${buildDir}/src/main/java")
        }
    }
}
