plugins {
    kotlin("jvm") version "2.1.0"
    kotlin("kapt") version "2.1.0"  // Kotlin Annotation Processing Tool
    id("java-library")
    jacoco
}

repositories {
    mavenCentral()
}

group = "io.github.robertomike"
version = "1.1.0"

// Test-only compatibility module: runs mvc's own source and test suite against
// an older Spring Boot line than mvc's 3.5.0, so a regression that only shows up
// on an older Boot/Hibernate/Spring Framework combo gets caught. Not published —
// no maven-publish plugin/block here.
//
// Target is 3.4.0, not the root module's nominally-declared 3.0.0 floor, and not
// the next obvious candidate 3.2.0 either:
//  - 3.0.0/3.1.x: already broken by mixed Spring Framework jar versions on the
//    classpath — the root module's own io.github.robertomike:spring-rules:2.0.9
//    dependency transitively pulls spring-boot-autoconfigure:3.2.0 (confirmed
//    via `./gradlew :mvc-boot3:dependencyInsight --dependency spring-beans`).
//  - 3.2.0/3.3.x (Spring Framework 6.1.x, confirmed resolved at 6.1.1 here):
//    hits a genuine Spring Framework bug, not ours — InvocableHandlerMethod's
//    KotlinDelegate mishandles Kotlin interface-default-method handlers with
//    generic type parameters (the same family as
//    https://github.com/spring-projects/spring-framework/issues/32510, whose
//    documented regression window is 6.1.5+, but the same code path already
//    misbehaves at 6.1.1 for our case: BulkOperationsMarker.bulkUpdate's nested
//    List<BulkUpdateItem<ID, UR>> deserializes UR as LinkedHashMap instead of
//    the concrete request type, and SoftDeletableMarker's default methods NPE
//    inside KotlinDelegate.invokeFunction before our code even runs). No
//    library-side fix exists — the bug is inside Spring's own reflection
//    dispatch, before SuperController's code executes. Fixed upstream by
//    Spring Framework 6.2.0 (Boot 3.4.0).
val springBootVersion = "3.4.0"
val baradumApacheVersion = "3.0.1"
val jdkCompileVersion = 17

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:${springBootVersion}")
    implementation("io.github.robertomike:baradum-core:${baradumApacheVersion}")
    implementation("io.github.robertomike:baradum-hefesto:${baradumApacheVersion}")
    // Boot 3.4.0 ships Hibernate 6.6 - the -hibernate-63 artifact family already
    // covers this range (mvc already uses it successfully at Boot 3.5.0's newer
    // Hibernate too).
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

// Reuse mvc's actual source and test suite verbatim - same code, same tests,
// different dependency versions. A failure here (with mvc green) is a real
// cross-version regression, not test drift between duplicated files.
sourceSets {
    main {
        kotlin.srcDir("../mvc/src/main/kotlin")
        resources.srcDir("../mvc/src/main/resources")
    }
    test {
        kotlin.srcDir("../mvc/src/test/kotlin")
        resources.srcDir("../mvc/src/test/resources")
    }
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

kotlin {
    jvmToolchain(17)
    compilerOptions {
        javaParameters = true
    }
}

tasks.withType(JavaCompile::class).configureEach {
    options.encoding = "UTF-8"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of("$jdkCompileVersion"))
    }
}
