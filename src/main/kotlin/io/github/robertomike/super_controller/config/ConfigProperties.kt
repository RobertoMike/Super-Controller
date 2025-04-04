package io.github.robertomike.super_controller.config

import io.github.robertomike.jakidate.validations.strings.cases.PascalCase
import io.github.robertomike.super_controller.validations.ClassSuffixConstraint
import io.github.robertomike.super_controller.validations.PathConstraint
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.jetbrains.annotations.NotNull
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.PropertySource
import org.springframework.validation.annotation.Validated

@ConfigurationProperties(prefix = "super-controller")
@PropertySource("classpath:super-controller.properties")
@Validated
class ConfigProperties {
    @field:NotNull
    @field:Valid
    val path = Path()
    @field:NotNull
    @field:NotBlank
    var prefixUrl = ""
    @field:PathConstraint
    var basePackage = ""
    @field:NotNull
    @field:Valid
    val classSuffix = ClassSuffix()
    @field:NotNull
    @field:Valid
    val controllerAdvice = ControllerAdvice()
}

class Path {
    @field:PathConstraint
    var requests: String = ""
    @field:PathConstraint
    var responses: String = ""
    @field:PathConstraint
    var policies: String = ""
    @field:PathConstraint
    var services: String = ""
}

class ClassSuffix {
    @field:PascalCase
    var request: String = ""
    @field:PascalCase
    var response: String = ""
    @field:PascalCase
    var policy: String = ""
    @field:PascalCase
    var service: String = ""
}

class ControllerAdvice {
    @field:NotNull
    var enable: Boolean = false
}