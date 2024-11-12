plugins {
    kotlin("jvm") version "2.0.21"

    id("java-library")
    `maven-publish`
    id("signing")
}

group = "io.github.robertomike"
version = "1.0.0"

val pomGroupId = group
val pomVersion = version
val artifactId = "super-controller"
val jdkCompileVersion = 17
val springBootVersion = "3.0.5"

repositories {
    mavenCentral()
}

dependencies {

    implementation("org.springframework.boot:spring-boot-starter-web:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:$springBootVersion")
    implementation("org.modelmapper:modelmapper:3.1.1")
    implementation("org.springframework.boot:spring-boot-starter-validation:$springBootVersion")
    implementation("jakarta.validation:jakarta.validation-api:3.0.2")
    implementation("io.github.robertomike:baradum-apache-tomcat:2.0.0")
    implementation("io.github.robertomike:hefesto-hibernate:2.0.0")
    implementation("org.atteo:evo-inflector:1.3")
    implementation("org.reflections:reflections:0.10.2")

    api("org.atteo:evo-inflector:1.3")
    api("org.reflections:reflections:0.10.2")
    api("org.modelmapper:modelmapper:3.1.1")
    api("org.springframework.boot:spring-boot-starter-validation:$springBootVersion")
    api("io.github.robertomike:baradum-apache-tomcat:2.0.0")
    api("io.github.robertomike:hefesto-hibernate:2.0.0")

    runtimeOnly("org.jetbrains.kotlin:kotlin-reflect:1.2.41")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation(kotlin("test"))
    testImplementation("org.springframework.boot:spring-boot-starter-test:$springBootVersion")
    testImplementation("mysql:mysql-connector-java:8.0.33")
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
            artifactId = artifactId
            version = "$pomVersion"

            pom {
                name = "Super controller"
                description = "This is a class for creation of controllers with super powers. Will create 5 default methods for API CRUD."
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