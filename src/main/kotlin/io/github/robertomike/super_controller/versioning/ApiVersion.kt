package io.github.robertomike.super_controller.versioning

/**
 * Annotation to mark a controller with API version information.
 *
 * This annotation provides metadata about the API version, deprecation status,
 * and sunset date for a controller.
 *
 * Example usage:
 * ```kotlin
 * @ApiVersion("v1", deprecated = false)
 * @RestController
 * class UserControllerV1 : SuperController<User, Long, StoreUserRequest, UpdateUserRequest>()
 *
 * @ApiVersion("v2", deprecated = true, sunset = "2026-12-31", documentationUrl = "https://api.example.com/docs/v2")
 * @RestController
 * class UserControllerV2 : SuperController<User, Long, StoreUserRequest, UpdateUserRequest>()
 * ```
 *
 * @property value The version identifier (e.g., "v1", "v2", "2023-10-01").
 * @property deprecated Whether this version is deprecated.
 * @property sunset ISO 8601 date when this version will be removed (e.g., "2026-12-31").
 * @property documentationUrl URL to the documentation for this version.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class ApiVersion(
    val value: String = "",
    val deprecated: Boolean = false,
    val sunset: String = "",
    val documentationUrl: String = ""
)
