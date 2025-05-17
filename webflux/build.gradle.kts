plugins {
    kotlin("jvm")
    kotlin("kapt") version "2.0.21"  // Kotlin Annotation Processing Tool
}

group = "io.github.robertomike"
version = "1.0.4"

repositories {
    mavenCentral()
}

val springBootVersion = "3.0.0"

dependencies {
    implementation(project(":"))
    implementation("org.springframework.boot:spring-boot-starter-webflux:${springBootVersion}")
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc:${springBootVersion}")

    // MapStruct core library
    implementation("org.mapstruct:mapstruct:1.6.3")

    // MapStruct annotation processor for code generation
    kapt("org.mapstruct:mapstruct-processor:1.6.3")

    testImplementation(kotlin("test"))
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