package io.github.robertomike.super_controller.config

import io.github.robertomike.super_controller.exceptions.SuperControllerException
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.PropertySource
import jakarta.annotation.PostConstruct

@ConfigurationProperties(prefix = "super-controller")
@PropertySource("classpath:super-controller.properties")
data class ConfigProperties(
    val path: Path = Path(),
    var prefixUrl: String = "",
    var basePackage: String = "",
    val classSuffix: ClassSuffix = ClassSuffix(),
    val controllerAdvice: ControllerAdvice = ControllerAdvice()
) {
    @PostConstruct
    fun init() {
        if (prefixUrl.isBlank()) {
            throw SuperControllerException("super-controller.prefix-url cannot be empty")
        }
        if (basePackage.isBlank()) {
            throw SuperControllerException("super-controller.base-package cannot be empty")
        }

        path.verify()
        classSuffix.verify()
    }
}

class Path {
    var requests: String = ""
    var responses: String = ""
    var policies: String = ""
    var services: String = ""

    fun verify() {
        if (requests.isBlank())
            throw SuperControllerException("super-controller.path.requests cannot be empty")
        if (responses.isBlank())
            throw SuperControllerException("super-controller.path.responses cannot be empty")
        if (policies.isBlank())
            throw SuperControllerException("super-controller.path.policies cannot be empty")
        if (services.isBlank())
            throw SuperControllerException("super-controller.path.services cannot be empty")
    }
}

class ClassSuffix {
    var request: String = ""
    var response: String = ""
    var policy: String = ""
    var service: String = ""

    fun verify() {
        if (request.isBlank())
            throw SuperControllerException("super-controller.class-suffix.request cannot be empty")
        if (response.isBlank())
            throw SuperControllerException("super-controller.class-suffix.response cannot be empty")
        if (policy.isBlank())
            throw SuperControllerException("super-controller.class-suffix.policy cannot be empty")
        if (service.isBlank())
            throw SuperControllerException("super-controller.class-suffix.service cannot be empty")
    }
}

class ControllerAdvice {
    var enable: Boolean = false
}