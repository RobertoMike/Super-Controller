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
val baradumApacheVersion = "2.1.1"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:${springBootVersion}")
    implementation("io.github.robertomike:baradum:${baradumApacheVersion}")
    implementation(project(":"))
    // MapStruct core library
    implementation("org.mapstruct:mapstruct:1.6.3")


    api("org.springframework.boot:spring-boot-starter-web:${springBootVersion}")


    // MapStruct annotation processor for code generation
    kapt("org.mapstruct:mapstruct-processor:1.6.3")

    testImplementation(kotlin("test"))
    testImplementation("mysql:mysql-connector-java:8.0.33")
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