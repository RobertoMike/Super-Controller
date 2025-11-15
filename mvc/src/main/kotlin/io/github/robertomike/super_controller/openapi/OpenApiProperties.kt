package io.github.robertomike.super_controller.openapi

import jakarta.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

/**
 * Configuration properties for OpenAPI documentation generation.
 *
 * These properties can be configured in your application.properties or application.yml file:
 *
 * ```properties
 * super-controller.openapi.enabled=true
 * super-controller.openapi.title=My API
 * super-controller.openapi.description=API for my application
 * super-controller.openapi.version=1.0.0
 * ```
 *
 * @property enabled Whether OpenAPI documentation generation is enabled (default: false)
 * @property title The title of the API documentation
 * @property description A description of the API
 * @property version The API version
 * @property servers List of server configurations for the API
 */
@ConfigurationProperties(prefix = "super-controller.openapi")
@Validated
data class OpenApiProperties(
    var enabled: Boolean = false,
    @NotBlank
    var title: String = "API Documentation",
    @NotBlank
    var description: String = "RESTful API built with Super-Controller",
    @NotBlank
    var version: String = "1.0.0",
    var servers: List<Server> = emptyList()
) {
    /**
     * Server configuration for OpenAPI documentation.
     *
     * @property url The server URL
     * @property description A description of the server
     */
    data class Server(
        var url: String = "",
        var description: String = ""
    )
}
