plugins {
    kotlin("jvm") version "2.1.0"

    kotlin("kapt") version "2.1.0"  // Kotlin Annotation Processing Tool
    id("java-library")
    id("com.vanniktech.maven.publish") version "0.29.0"
    jacoco
}

group = "io.github.robertomike"
version = "1.0.7"

val baseArtifactId = "super-controller"
val jdkCompileVersion = 17
val springVersion = "6.0.0"
val springBootVersion = "3.0.0"
val springRules = "2.0.9"
val mapStruct = "1.6.3"

repositories {
    mavenCentral()
}

dependencies {
    api("io.github.robertomike:spring-rules:${springRules}")
    api("org.springframework.data:spring-data-commons:$springBootVersion")
    api("org.springframework:spring-web:${springVersion}")
    api("org.springframework:spring-tx:${springVersion}")
    api("org.mapstruct:mapstruct:$mapStruct")
    api("org.atteo:evo-inflector:1.3")
    api("org.reflections:reflections:0.10.2")
    api("com.fasterxml.jackson.module:jackson-module-kotlin:2.20.1")

    runtimeOnly("org.jetbrains.kotlin:kotlin-reflect:1.2.41")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation(kotlin("test"))
}


kapt {
    arguments {
        // MapStruct configuration options
        arg("mapstruct.defaultComponentModel", "spring")
        arg("mapstruct.unmappedTargetPolicy", "IGNORE")
    }
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.80".toBigDecimal()
            }
        }
    }
}

kotlin {
    jvmToolchain(17)
}


mavenPublishing {
    publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)

    // Only sign if credentials are available (CI environment)
    if (project.hasProperty("signing.keyId")) {
        signAllPublications()
    }

    coordinates(
        groupId = group.toString(),
        artifactId = baseArtifactId,
        version = version.toString()
    )

    pom {
        name.set("Super controller")
        description.set("This is a class for creation of controllers with super powers. Will create 5 default methods for API CRUD.")
        url.set("https://github.com/RobertoMike/SuperController")
        inceptionYear.set("2024")

        licenses {
            license {
                name.set("MIT License")
                url.set("http://www.opensource.org/licenses/mit-license.php")
            }
        }

        developers {
            developer {
                id.set("robertomike")
                name.set("Roberto Micheletti")
                email.set("rmworking@hotmail.com")
                url.set("https://github.com/RobertoMike")
            }
        }

        scm {
            connection.set("scm:git:git://github.com/RobertoMike/SuperController.git")
            developerConnection.set("scm:git:ssh://git@github.com/RobertoMike/SuperController.git")
            url.set("https://github.com/RobertoMike/SuperController")
        }
    }
}


tasks.withType(JavaCompile::class).configureEach {
    options.encoding = "UTF-8"
}

tasks.register("printVersion") {
    doLast {
        println(project.version)
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of("$jdkCompileVersion"))
    }
}