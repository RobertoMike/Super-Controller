plugins {
    kotlin("jvm") version "2.0.21"

    kotlin("kapt") version "2.0.21"  // Kotlin Annotation Processing Tool
    id("java-library")
    `maven-publish`
    id("signing")
}

group = "io.github.robertomike"
version = "1.0.4"

val pomGroupId = group
val pomVersion = version
val baseArtifactId = "super-controller"
val jdkCompileVersion = 17
val springVersion = "6.0.0"
val springBootVersion = "3.0.0"
val springRules = "2.0.8"
val baradumApacheVersion = "2.0.2"
val jakartaVersion = "3.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("jakarta.validation:jakarta.validation-api:${jakartaVersion}")
    implementation("org.springframework.data:spring-data-commons:$springBootVersion")

    implementation("org.atteo:evo-inflector:1.3")
    implementation("org.reflections:reflections:0.10.2")
    // MapStruct core library
    implementation("org.mapstruct:mapstruct:1.6.3")

    // MapStruct annotation processor for code generation
    kapt("org.mapstruct:mapstruct-processor:1.6.3")

    api("org.mapstruct:mapstruct:1.6.3")
    api("com.fasterxml.jackson.core:jackson-databind:2.19.0")
    api("org.atteo:evo-inflector:1.3")
    api("org.reflections:reflections:0.10.2")
    api("org.springframework:spring-web:${springVersion}")

    api("io.github.robertomike:spring-rules:$springRules") {
        exclude(group = "org.springframework", module = "spring-webmvc")
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-web")
    }

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