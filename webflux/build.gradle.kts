plugins {
    kotlin("jvm")
    kotlin("kapt") version "2.1.0"  // Kotlin Annotation Processing Tool
    id("java-library")
    `maven-publish`
    id("signing")
}

repositories {
    mavenCentral()
}

group = "io.github.robertomike"
version = "1.0.0"

val pomGroupId = group
val pomVersion = version
val baseArtifactId = "super-controller-webflux"
val springBootVersion = "3.0.0"
val jdkCompileVersion = 17

dependencies {
    implementation(project(":"))
    implementation("org.springframework.boot:spring-boot-starter-webflux:${springBootVersion}")
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc:${springBootVersion}")

    // MapStruct core library
    implementation("org.mapstruct:mapstruct:1.6.3")

    // MapStruct annotation processor for code generation
    kapt("org.mapstruct:mapstruct-processor:1.6.3")

    testImplementation(kotlin("test"))
    testImplementation("org.springframework.boot:spring-boot-starter-validation:${springBootVersion}")
    testImplementation("io.asyncer:r2dbc-mysql:1.4.1")
    testImplementation("io.projectreactor:reactor-test:3.7.6")
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
}
kotlin {
    jvmToolchain(17)
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