package io.github.robertomike.super_controller.versioning

import jakarta.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

/**
 * Configuration properties for API versioning.
 *
 * These properties can be configured in your application.properties or application.yml file:
 *
 * ```properties
 * super-controller.versioning.enabled=true
 * super-controller.versioning.default-version=v1
 * super-controller.versioning.strategy=URI
 * super-controller.versioning.add-deprecation-headers=true
 * super-controller.versioning.header-name=X-API-Version
 * super-controller.versioning.param-name=version
 * super-controller.versioning.media-type-prefix=application/vnd.api
 * ```
 *
 * @property enabled Whether API versioning is enabled (default: false)
 * @property defaultVersion The default version to use when none is specified
 * @property strategy The strategy for extracting version from request
 * @property headerName The header name when using HEADER strategy
 * @property paramName The parameter name when using PARAMETER strategy
 * @property mediaTypePrefix The media type prefix when using ACCEPT_HEADER strategy
 * @property addDeprecationHeaders Whether to add deprecation headers to responses
 */
@ConfigurationProperties(prefix = "super-controller.versioning")
@Validated
data class VersioningProperties(
    var enabled: Boolean = false,
    @NotBlank
    var defaultVersion: String = "v1",
    @NotBlank
    var strategy: VersionStrategy = VersionStrategy.URI,
    @NotBlank
    var headerName: String = "X-API-Version",
    @NotBlank
    var paramName: String = "version",
    @NotBlank
    var mediaTypePrefix: String = "application/vnd.api",
    var addDeprecationHeaders: Boolean = true
)