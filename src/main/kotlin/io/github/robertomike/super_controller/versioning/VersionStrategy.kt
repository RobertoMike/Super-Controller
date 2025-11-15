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
    ACCEPT_HEADER;

    fun isUri(): Boolean {
        return this == URI
    }
}