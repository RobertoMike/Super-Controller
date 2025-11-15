package io.github.robertomike.super_controller.versioning

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Auto-configuration for API versioning.
 *
 * This configuration is automatically enabled when:
 * - `super-controller.versioning.enabled=true` is set in application properties
 *
 * It provides:
 * - [ApiVersionInterceptor] automatically registered to add deprecation headers
 * - [ApiVersionRequestMappingHandlerMapping] for non-URI strategies (HEADER, PARAMETER, ACCEPT_HEADER)
 *
 * Example configuration in application.properties:
 * ```properties
 * super-controller.versioning.enabled=true
 * super-controller.versioning.default-version=v1
 * super-controller.versioning.strategy=URI
 * super-controller.versioning.add-deprecation-headers=true
 * ```
 *
 * ## Routing Strategies
 *
 * ### URI Strategy (Default)
 * Version is part of the URL path. Controllers must include version in @RequestMapping:
 * ```kotlin
 * @ApiVersion("v1")
 * @RequestMapping("/v1/users")
 * class UserControllerV1 : SuperController<...>()
 * ```
 * Matches: GET /v1/users
 *
 * ### HEADER Strategy
 * Version is specified in a custom header. Controllers can omit version from URL:
 * ```kotlin
 * @ApiVersion("v1")
 * @RequestMapping("/users")
 * class UserControllerV1 : SuperController<...>()
 * ```
 * Matches: GET /users with header X-API-Version: v1
 *
 * ### PARAMETER Strategy
 * Version is specified as a query parameter:
 * ```kotlin
 * @ApiVersion("v1")
 * @RequestMapping("/users")
 * class UserControllerV1 : SuperController<...>()
 * ```
 * Matches: GET /users?version=v1
 *
 * ### ACCEPT_HEADER Strategy
 * Version is specified in Accept header using vendor media type:
 * ```kotlin
 * @ApiVersion("v1")
 * @RequestMapping("/users")
 * class UserControllerV1 : SuperController<...>()
 * ```
 * Matches: GET /users with Accept: application/vnd.api.v1+json
 *
 * To disable the interceptor while keeping other versioning features:
 * ```properties
 * super-controller.versioning.enabled=true
 * super-controller.versioning.add-deprecation-headers=false
 * ```
 */
@Configuration
@EnableConfigurationProperties(VersioningProperties::class)
@ConditionalOnProperty(
    prefix = "super-controller.versioning",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = false
)
open class VersioningAutoConfiguration(
    private val properties: VersioningProperties
) : WebMvcConfigurer {
    /**
     * Creates the API version interceptor bean.
     */
    @Bean
    open fun apiVersionInterceptor(): ApiVersionInterceptor {
        return ApiVersionInterceptor(properties)
    }

    /**
     * Creates custom request mapping handler for non-URI strategies.
     *
     * This handler enables routing based on headers, parameters, or Accept header
     * instead of just URL paths.
     *
     * Note: For URI strategy, standard Spring @RequestMapping works fine, so we only
     * create this bean for other strategies.
     */
    @Bean
    open fun apiVersionRequestMappingHandlerMapping(): ApiVersionRequestMappingHandlerMapping? {
        // Only create for non-URI strategies
        return if (properties.strategy != VersionStrategy.URI) {
            ApiVersionRequestMappingHandlerMapping(properties)
        } else {
            null
        }
    }

    /**
     * Registers the API version interceptor to add deprecation headers.
     *
     * The interceptor is only active when `addDeprecationHeaders` is true.
     */
    override fun addInterceptors(registry: InterceptorRegistry) {
        if (properties.addDeprecationHeaders) {
            registry.addInterceptor(apiVersionInterceptor())
        }
    }
}
