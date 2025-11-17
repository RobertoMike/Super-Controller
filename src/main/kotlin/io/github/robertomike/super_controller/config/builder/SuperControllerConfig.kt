package io.github.robertomike.super_controller.config.builder

import io.github.robertomike.super_controller.enums.Methods
import io.github.robertomike.super_controller.policies.BasePolicy
import io.github.robertomike.super_controller.requests.Request
import io.github.robertomike.super_controller.services.interfaces.BasicService
import org.springframework.data.domain.Page
import org.springframework.http.HttpMethod
import org.springframework.web.bind.annotation.RequestMethod

/**
 * Configuration for caching behavior in SuperController.
 *
 * @property enabled Whether caching is enabled.
 * @property cacheName The name of the cache.
 * @property ttl Time to live in seconds.
 * @property saveIndex Whether to cache index/list results.
 * @property saveSingle Whether to cache single entity results.
 * @property saveOnStore Whether to cache entities on creation.
 */
data class CacheConfig(
    val enabled: Boolean = true,
    val cacheName: String,
    val ttl: Long = 3600,
    val saveIndex: Boolean = true,
    val saveSingle: Boolean = true,
    val saveOnStore: Boolean = false
)

/**
 * Definition of a custom route.
 *
 * @property path The URL path for the route.
 * @property method The HTTP method for the route.
 * @property handlerMethod The name of the method that handles this route.
 * @property description Optional description of what this route does.
 */
data class RouteDefinition(
    val path: String,
    val method: RequestMethod,
    val handlerMethod: String,
    val description: String = ""
)

/**
 * Complete configuration for a SuperController.
 *
 * This is the result of building a configuration using [SuperControllerBuilder].
 *
 * @param M The type of the model.
 * @param ID The type of the ID.
 * @param SR The type of the store request.
 * @param UR The type of the update request.
 */
data class SuperControllerConfig<M, ID, SR, UR>(
    val service: BasicService<M, Page<M>, ID, Request, Request, Unit>?,
    val needAuthorization: Boolean,
    val policy: BasePolicy<M, Request, Request, *>?,
    val basePackage: String,
    val mapper: Any?,
    val cacheConfig: CacheConfig?,
    val onlyUrls: List<Methods>?,
    val exceptUrls: List<Methods>?,
    val customRoutes: Map<String, RouteDefinition>,
    val enableSoftDelete: Boolean = false,
    val enableBulkOperations: Boolean = false,
    val apiVersion: String? = null
) {
    /**
     * Validates the configuration.
     *
     * @throws IllegalStateException if configuration is invalid.
     */
    fun validate() {
        require(basePackage.isNotBlank()) { "Base package cannot be blank" }

        if (onlyUrls != null && exceptUrls != null) {
            throw IllegalStateException("Cannot specify both onlyUrls and exceptUrls")
        }

        if (onlyUrls != null && onlyUrls.isEmpty()) {
            throw IllegalStateException("onlyUrls cannot be empty when specified")
        }

        if (exceptUrls != null && exceptUrls.isEmpty()) {
            throw IllegalStateException("exceptUrls cannot be empty when specified")
        }

        cacheConfig?.let {
            require(it.cacheName.isNotBlank()) { "Cache name cannot be blank" }
            require(it.ttl > 0) { "Cache TTL must be positive" }
        }

        customRoutes.forEach { (_, route) ->
            require(route.path.isNotBlank()) { "Route path cannot be blank" }
            require(route.handlerMethod.isNotBlank()) { "Route handler method cannot be blank" }
        }
    }
}
