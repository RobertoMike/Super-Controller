package io.github.robertomike.super_controller.versioning

import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.servlet.mvc.condition.RequestCondition

/**
 * Custom request condition that matches requests based on API version.
 *
 * This condition extracts the API version from the request using the configured
 * strategy (URI, HEADER, PARAMETER, or ACCEPT_HEADER) and matches it against
 * the expected version from the [@ApiVersion][ApiVersion] annotation.
 *
 * Example usage with custom request mapping:
 * ```kotlin
 * @ApiVersion("v1")
 * @RestController
 * @RequestMapping("/users") // No version in URL
 * class UserControllerV1 : SuperController<...>()
 *
 * // With strategy=HEADER, matches requests with:
 * // X-API-Version: v1
 * ```
 *
 * @property version The expected API version (e.g., "v1", "v2")
 * @property config The versioning configuration containing the strategy
 */
class ApiVersionRequestCondition(
    private val version: String,
    private val config: VersioningConfig
) : RequestCondition<ApiVersionRequestCondition> {

    /**
     * Combines this condition with another condition.
     * Returns the more specific condition (prefers the one with a version).
     */
    override fun combine(other: ApiVersionRequestCondition): ApiVersionRequestCondition {
        // If other has a version, prefer it (more specific)
        return if (other.version.isNotBlank()) other else this
    }

    /**
     * Compares this condition to another for sorting.
     * Conditions with versions are considered more specific.
     */
    override fun compareTo(other: ApiVersionRequestCondition, request: HttpServletRequest): Int {
        // Prefer conditions with versions
        return when {
            version.isNotBlank() && other.version.isBlank() -> -1
            version.isBlank() && other.version.isNotBlank() -> 1
            else -> 0
        }
    }

    /**
     * Gets the matching condition for the given request.
     * Returns this condition if the request version matches, null otherwise.
     */
    override fun getMatchingCondition(request: HttpServletRequest): ApiVersionRequestCondition? {
        val requestVersion = extractVersionFromRequest(request)
        
        // If no version in request, use default version
        val effectiveVersion = requestVersion ?: config.defaultVersion
        
        // Match if versions are equal
        return if (effectiveVersion == version) this else null
    }

    /**
     * Extracts the API version from the request based on the configured strategy.
     */
    private fun extractVersionFromRequest(request: HttpServletRequest): String? {
        return when (config.strategy) {
            VersionStrategy.URI -> extractVersionFromUri(request)
            VersionStrategy.HEADER -> request.getHeader(config.headerName)
            VersionStrategy.PARAMETER -> request.getParameter(config.paramName)
            VersionStrategy.ACCEPT_HEADER -> extractVersionFromAcceptHeader(request)
        }
    }

    /**
     * Extracts version from URI path (e.g., /v1/users -> v1).
     */
    private fun extractVersionFromUri(request: HttpServletRequest): String? {
        val path = request.requestURI
        val versionPattern = Regex("""/v(\d+)(?:/|${'$'})""")
        val match = versionPattern.find(path)
        return match?.let { "v${it.groupValues[1]}" }
    }

    /**
     * Extracts version from Accept header (e.g., application/vnd.api.v1+json -> v1).
     */
    private fun extractVersionFromAcceptHeader(request: HttpServletRequest): String? {
        val acceptHeader = request.getHeader("Accept") ?: return null
        val versionPattern = Regex("""${Regex.escape(config.mediaTypePrefix)}\.v(\d+)""")
        val match = versionPattern.find(acceptHeader)
        return match?.let { "v${it.groupValues[1]}" }
    }

    override fun toString(): String {
        return "ApiVersionRequestCondition(version='$version', strategy=${config.strategy})"
    }
}
