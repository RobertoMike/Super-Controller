plugins {
    kotlin("jvm") version "2.1.0"
    kotlin("kapt") version "2.1.0"  // Kotlin Annotation Processing Tool
    id("java-library")
    `maven-publish`
    id("signing")
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
val baradumApacheVersion = "2.1.1"
val jdkCompileVersion = 17

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:${springBootVersion}")
    implementation("io.github.robertomike:baradum:${baradumApacheVersion}")
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


publishing {
    publications {
        register("library", MavenPublication::class) {
            from(components["java"])

            groupId = "$pomGroupId"
            artifactId = baseArtifactId
            version = "$pomVersion"

            pom {
                name = "Super controller"
                description =
                    "This is a class for creation of controllers with super powers. Will create 5 default methods for API CRUD."
                url = "https://github.com/RobertoMike/SuperController"
                inceptionYear = "2024"

                licenses {
                    license {
                        name = "MIT License"
                        url = "http://www.opensource.org/licenses/mit-license.php"
                    }
                }
                developers {
                    developer {
                        name = "Roberto Micheletti"
                        email = "rmworking@hotmail.com"
                        organization = "Roberto Micheletti"
                        organizationUrl = "https://github.com/RobertoMike"
                    }
                }
                scm {
                    connection = "scm:git:git://github.com/RobertoMike/SuperController.git"
                    developerConnection = "scm:git:ssh://github.com:RobertoMike/SuperController.git"
                    url = "https://github.com/RobertoMike/SuperController"
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
            metadataSources {
                gradleMetadata()
            }
        }
    }
}

java {
    withJavadocJar()
    withSourcesJar()
}

tasks.withType(JavaCompile::class).configureEach {
    options.encoding = "UTF-8"
}

if (!project.hasProperty("local")) {
    signing {
        setRequired { !version.toString().endsWith("SNAPSHOT") }
        sign(publishing.publications["library"])
    }
}

tasks.register("printVersion") {
    doLast {
        println(project.version)
    }
}

java {
    withJavadocJar()
    withSourcesJar()
    toolchain {
        languageVersion.set(JavaLanguageVersion.of("$jdkCompileVersion"))
    }
}