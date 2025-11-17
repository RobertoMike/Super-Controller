package io.github.robertomike.super_controller.config.builder

import io.github.robertomike.super_controller.enums.Methods
import io.github.robertomike.super_controller.policies.BasePolicy
import io.github.robertomike.super_controller.requests.Request
import io.github.robertomike.super_controller.services.interfaces.BasicService
import org.springframework.data.domain.Page
import org.springframework.web.bind.annotation.RequestMethod

/**
 * Fluent builder for creating SuperController configurations.
 *
 * This builder provides a clean, type-safe way to configure SuperController instances
 * with all available options. It follows the builder pattern to allow method chaining.
 *
 * Example usage:
 * ```kotlin
 * @RestController
 * class UserController : SuperController<User, Long, StoreUserRequest, UpdateUserRequest>() {
 *
 *     @Autowired
 *     lateinit var userService: UserService
 *
 *     @Autowired
 *     lateinit var userMapper: UserResponseMapper
 *
 *     @PostConstruct
 *     fun configure() {
 *         val config = SuperControllerBuilder<User, Long, StoreUserRequest, UpdateUserRequest>()
 *             .withService(userService)
 *             .withBasePackage("io.github.robertomike.super_controller")
 *             .withAuthorization(true)
 *             .withMapper(userMapper)
 *             .withCache(CacheConfig(
 *                 cacheName = "users",
 *                 ttl = 1800
 *             ))
 *             .only(Methods.INDEX, Methods.SHOW, Methods.STORE)
 *             .addCustomRoute("/users/{id}/activate", RequestMethod.POST, "activate")
 *             .enableSoftDelete()
 *             .enableBulkOperations()
 *             .build()
 *
 *         applyConfig(config)
 *     }
 *
 *     fun activate(@PathVariable id: Long): UserResponse {
 *         // Custom implementation
 *     }
 * }
 * ```
 *
 * @param M The type of the model.
 * @param ID The type of the ID.
 * @param SR The type of the store request.
 * @param UR The type of the update request.
 */
class SuperControllerBuilder<M, ID : Any, SR : Request, UR : Request> {
    private var service: BasicService<M, Page<M>, ID, Request, Request, Unit>? = null
    private var needAuthorization: Boolean = true
    private var policy: BasePolicy<M, Request, Request, *>? = null
    private var basePackage: String? = null
    private var mapper: Any? = null
    private var cacheConfig: CacheConfig? = null
    private var onlyUrls: List<Methods>? = null
    private var exceptUrls: List<Methods>? = null
    private var customRoutes: MutableMap<String, RouteDefinition> = mutableMapOf()
    private var enableSoftDelete: Boolean = false
    private var enableBulkOperations: Boolean = false
    private var apiVersion: String? = null

    /**
     * Sets the service to be used by the controller.
     *
     * @param service The service instance.
     * @return This builder for chaining.
     */
    fun withService(service: BasicService<M, Page<M>, ID, Request, Request, Unit>) = apply {
        this.service = service
    }

    /**
     * Configures authorization requirement.
     *
     * @param enable Whether authorization is required (default: true).
     * @return This builder for chaining.
     */
    fun withAuthorization(enable: Boolean = true) = apply {
        this.needAuthorization = enable
    }

    /**
     * Sets the policy to be used for authorization.
     *
     * @param policy The policy instance.
     * @return This builder for chaining.
     */
    fun withPolicy(policy: BasePolicy<M, Request, Request, *>) = apply {
        this.policy = policy
    }

    /**
     * Sets the base package for component scanning.
     *
     * @param basePackage The base package path.
     * @return This builder for chaining.
     */
    fun withBasePackage(basePackage: String) = apply {
        this.basePackage = basePackage
    }

    /**
     * Sets the response mapper for transforming entities.
     *
     * @param mapper The mapper instance.
     * @return This builder for chaining.
     */
    fun withMapper(mapper: Any) = apply {
        this.mapper = mapper
    }

    /**
     * Configures caching behavior.
     *
     * @param config The cache configuration.
     * @return This builder for chaining.
     */
    fun withCache(config: CacheConfig) = apply {
        this.cacheConfig = config
    }

    /**
     * Specifies which endpoints to enable. All other endpoints will be disabled.
     * Cannot be used together with [except].
     *
     * @param methods The methods to enable.
     * @return This builder for chaining.
     */
    fun only(vararg methods: Methods) = apply {
        require(exceptUrls == null) { "Cannot use both only() and except()" }
        this.onlyUrls = methods.toList()
    }

    /**
     * Specifies which endpoints to disable. All other endpoints will be enabled.
     * Cannot be used together with [only].
     *
     * @param methods The methods to disable.
     * @return This builder for chaining.
     */
    fun except(vararg methods: Methods) = apply {
        require(onlyUrls == null) { "Cannot use both only() and except()" }
        this.exceptUrls = methods.toList()
    }

    /**
     * Adds a custom route to the controller.
     *
     * @param path The URL path for the route.
     * @param method The HTTP method.
     * @param handler The name of the handler method.
     * @param description Optional description.
     * @return This builder for chaining.
     */
    fun addCustomRoute(
        path: String,
        method: RequestMethod,
        handler: String,
        description: String = ""
    ) = apply {
        customRoutes[path] = RouteDefinition(path, method, handler, description)
    }

    /**
     * Enables soft delete functionality.
     *
     * @return This builder for chaining.
     */
    fun enableSoftDelete() = apply {
        this.enableSoftDelete = true
    }

    /**
     * Disables soft delete functionality.
     *
     * @return This builder for chaining.
     */
    fun disableSoftDelete() = apply {
        this.enableSoftDelete = false
    }

    /**
     * Enables bulk operations.
     *
     * @return This builder for chaining.
     */
    fun enableBulkOperations() = apply {
        this.enableBulkOperations = true
    }

    /**
     * Disables bulk operations.
     *
     * @return This builder for chaining.
     */
    fun disableBulkOperations() = apply {
        this.enableBulkOperations = false
    }

    /**
     * Sets the API version for this controller.
     *
     * @param version The version string (e.g., "v1", "v2").
     * @return This builder for chaining.
     */
    fun withApiVersion(version: String) = apply {
        this.apiVersion = version
    }

    /**
     * Builds and returns the configuration.
     *
     * @return The built configuration.
     * @throws IllegalStateException if required configuration is missing.
     */
    fun build(): SuperControllerConfig<M, ID, SR, UR> {
        require(basePackage != null) { "Base package is required. Use withBasePackage()" }

        val config = SuperControllerConfig<M, ID, SR, UR>(
            service = service,
            needAuthorization = needAuthorization,
            policy = policy,
            basePackage = basePackage!!,
            mapper = mapper,
            cacheConfig = cacheConfig,
            onlyUrls = onlyUrls,
            exceptUrls = exceptUrls,
            customRoutes = customRoutes.toMap(),
            enableSoftDelete = enableSoftDelete,
            enableBulkOperations = enableBulkOperations,
            apiVersion = apiVersion
        )

        config.validate()

        return config
    }

    companion object {
        /**
         * Creates a new builder instance.
         *
         * @return A new builder.
         */
        fun <M, ID : Any, SR : Request, UR : Request> create():
                SuperControllerBuilder<M, ID, SR, UR> = SuperControllerBuilder()
    }
}
