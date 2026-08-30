plugins {
    kotlin("jvm") version "2.1.0"
    kotlin("kapt") version "2.1.0"  // Kotlin Annotation Processing Tool
    id("java-library")
    id("com.vanniktech.maven.publish") version "0.29.0"
    jacoco
}

repositories {
    mavenCentral()
}

group = "io.github.robertomike"
version = "1.1.0"

val pomGroupId = group
val pomVersion = version
val baseArtifactId = "super-controller-mvc"
val springBootVersion = "3.5.0"
val baradumApacheVersion = "3.0.1"
val jdkCompileVersion = 17

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:${springBootVersion}")
    implementation("io.github.robertomike:baradum-core:${baradumApacheVersion}")
    implementation("io.github.robertomike:baradum-hefesto:${baradumApacheVersion}")
    implementation("io.hypersistence:hypersistence-utils-hibernate-63:3.12.0")
    // MapStruct core library
    implementation("org.mapstruct:mapstruct:1.6.3")

    // OpenAPI / Swagger Documentation
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")

    api(project(":"))
    api("org.springframework.boot:spring-boot-starter-web:${springBootVersion}")

    // MapStruct annotation processor for code generation
    kapt("org.mapstruct:mapstruct-processor:1.6.3")

    testImplementation(kotlin("test"))
    testImplementation("mysql:mysql-connector-java:8.0.33")
    testImplementation("com.h2database:h2:2.2.224")
    testImplementation("org.springframework.boot:spring-boot-starter-test:${springBootVersion}")
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
    compilerOptions {
        javaParameters = true
    }
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
        name.set("Super controller MVC")
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