package io.github.robertomike.super_controller.versioning

/**
 * Strategy for determining API version from HTTP request.
 */
enum class VersionStrategy {
    /**
     * Version specified in URI path (e.g., /v1/users, /v2/users).
     * This is the most common and recommended approach.
     */
    URI,

    /**
     * Version specified in custom header (e.g., X-API-Version: v1).
     * Useful when you want clean URLs without version information.
     */
    HEADER,

    /**
     * Version specified as query parameter (e.g., /users?version=v1).
     * Less common but useful for simple implementations.
     */
    PARAMETER,

    /**
     * Version specified in Accept header using vendor media type
     * (e.g., Accept: application/vnd.api.v1+json).
     * RESTful approach following RFC 6838.
     */
    ACCEPT_HEADER
}

/**
 * Configuration for API versioning behavior.
 *
 * Example usage:
 * ```kotlin
 * @Configuration
 * class ApiConfig {
 *     @Bean
 *     fun versioningConfig() = VersioningConfig(
 *         defaultVersion = "v1",
 *         strategy = VersionStrategy.URI,
 *         headerName = "X-API-Version"
 *     )
 * }
 * ```
 *
 * @property defaultVersion The default version to use when none is specified.
 * @property strategy The strategy for extracting version from request.
 * @property headerName The header name when using HEADER strategy.
 * @property paramName The parameter name when using PARAMETER strategy.
 * @property mediaTypePrefix The media type prefix when using ACCEPT_HEADER strategy.
 * @property addDeprecationHeaders Whether to add deprecation headers to responses.
 */
data class VersioningConfig(
    var defaultVersion: String = "v1",
    var strategy: VersionStrategy = VersionStrategy.URI,
    var headerName: String = "X-API-Version",
    var paramName: String = "version",
    var mediaTypePrefix: String = "application/vnd.api",
    var addDeprecationHeaders: Boolean = true
) {
    /**
     * Validates the configuration.
     *
     * @throws IllegalStateException if configuration is invalid.
     */
    fun validate() {
        require(defaultVersion.isNotBlank()) { "Default version cannot be blank" }
        require(headerName.isNotBlank()) { "Header name cannot be blank" }
        require(paramName.isNotBlank()) { "Parameter name cannot be blank" }
        require(mediaTypePrefix.isNotBlank()) { "Media type prefix cannot be blank" }
    }
}
