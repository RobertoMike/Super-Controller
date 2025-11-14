package io.github.robertomike.super_controller.versioning

import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.web.servlet.mvc.condition.RequestCondition
import org.springframework.web.servlet.mvc.method.RequestMappingInfo
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import java.lang.reflect.Method

/**
 * Custom request mapping handler that adds version-aware routing.
 *
 * This handler extends Spring's default [RequestMappingHandlerMapping] to support
 * routing requests based on API version extracted from headers, parameters, or Accept header,
 * not just URL paths.
 *
 * When a controller is annotated with [@ApiVersion][ApiVersion], this handler creates
 * a custom [ApiVersionRequestCondition] that matches requests based on the configured
 * [VersionStrategy].
 *
 * This is automatically registered when:
 * ```properties
 * super-controller.versioning.enabled=true
 * super-controller.versioning.strategy=HEADER  # or PARAMETER, ACCEPT_HEADER
 * ```
 *
 * Example:
 * ```kotlin
 * // With strategy=HEADER
 * @ApiVersion("v1")
 * @RestController
 * @RequestMapping("/users")
 * class UserControllerV1 : SuperController<...>()
 *
 * // Matches requests with header: X-API-Version: v1
 * ```
 *
 * @property config The versioning configuration
 */
class ApiVersionRequestMappingHandlerMapping(
    private val config: VersioningConfig
) : RequestMappingHandlerMapping() {

    init {
        // Set order to run before default handler mapping
        order = -1
    }

    /**
     * Creates a custom request condition for methods with @ApiVersion annotation.
     */
    override fun getCustomMethodCondition(method: Method): RequestCondition<*>? {
        return createCondition(method.declaringClass)
    }

    /**
     * Creates a custom request condition for types (classes) with @ApiVersion annotation.
     */
    override fun getCustomTypeCondition(handlerType: Class<*>): RequestCondition<*>? {
        return createCondition(handlerType)
    }

    /**
     * Creates an ApiVersionRequestCondition if the class/method has @ApiVersion annotation.
     */
    private fun createCondition(element: Class<*>): ApiVersionRequestCondition? {
        val apiVersion = AnnotatedElementUtils.findMergedAnnotation(element, ApiVersion::class.java)
        return apiVersion?.let {
            ApiVersionRequestCondition(it.value, config)
        }
    }

    override fun toString(): String {
        return "ApiVersionRequestMappingHandlerMapping(strategy=${config.strategy})"
    }
}
