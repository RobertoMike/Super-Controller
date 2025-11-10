plugins {
    kotlin("jvm") version "2.0.21"

    kotlin("kapt") version "2.0.21"  // Kotlin Annotation Processing Tool
    id("java-library")
    id("com.vanniktech.maven.publish") version "0.29.0"
}

group = "io.github.robertomike"
version = "1.0.5"

val pomGroupId = group
val pomVersion = version
val baseArtifactId = "super-controller"
val jdkCompileVersion = 17
val springBootVersion = "3.0.0"
val baradumApacheVersion = "2.0.2"
val springRules = "2.0.5"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-validation:$springBootVersion")
    implementation("io.github.robertomike:baradum-apache-tomcat:$baradumApacheVersion")
    implementation("io.github.robertomike:spring-rules:$springRules")
    implementation("org.atteo:evo-inflector:1.3")
    implementation("org.reflections:reflections:0.10.2")
    // MapStruct core library
    implementation("org.mapstruct:mapstruct:1.6.3")

    // MapStruct annotation processor for code generation
    kapt("org.mapstruct:mapstruct-processor:1.6.3")

    api("org.mapstruct:mapstruct:1.6.3")
    api("org.atteo:evo-inflector:1.3")
    api("org.reflections:reflections:0.10.2")
    api("org.springframework.boot:spring-boot-starter-validation:$springBootVersion")
    api("io.github.robertomike:spring-rules:$springRules")

    runtimeOnly("org.jetbrains.kotlin:kotlin-reflect:1.2.41")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation(kotlin("test"))
    testImplementation("org.springframework.boot:spring-boot-starter-test:$springBootVersion")
    testImplementation("mysql:mysql-connector-java:8.0.33")
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