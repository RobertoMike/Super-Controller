package io.github.robertomike.super_controller.annotations

/**
 * Annotation for adding OpenAPI/Swagger documentation metadata to Super-Controller controllers.
 *
 * This annotation can be used to provide custom documentation for your API endpoints.
 * It works in conjunction with SpringDoc OpenAPI to generate comprehensive API documentation.
 *
 * Example usage:
 * ```kotlin
 * @SuperControllerOpenApi(
 *     summary = "User Management API",
 *     description = "Endpoints for managing user resources with full CRUD operations",
 *     tags = ["Users", "Authentication"]
 * )
 * @RestController
 * class UserController : SuperController<User, Long, CreateUserRequest, UpdateUserRequest>()
 * ```
 *
 * @property summary A brief summary of what this controller does
 * @property description A detailed description of the controller's functionality
 * @property tags Custom tags to group related operations in the OpenAPI documentation
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class SuperControllerOpenApi(
    val summary: String = "",
    val description: String = "",
    val tags: Array<String> = []
)
