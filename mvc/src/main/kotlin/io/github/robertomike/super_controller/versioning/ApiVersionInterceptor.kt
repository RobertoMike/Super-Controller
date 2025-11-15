package io.github.robertomike.super_controller.versioning

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor

/**
 * Interceptor that adds deprecation headers to API responses.
 *
 * This interceptor checks if the controller handling the request is annotated
 * with [@ApiVersion][ApiVersion] and if it's marked as deprecated. If so, it adds
 * appropriate headers to warn clients about the deprecation.
 *
 * Headers added:
 * - X-API-Deprecated: true
 * - X-API-Sunset: {date} (if sunset date is specified)
 * - X-API-Version: {version}
 * - Link: {documentation_url}; rel="deprecation" (if documentation URL is specified)
 *
 * This interceptor is automatically registered when versioning is enabled in application.properties:
 * ```properties
 * super-controller.versioning.enabled=true
 * super-controller.versioning.add-deprecation-headers=true
 * ```
 *
 * To disable only the deprecation headers while keeping versioning:
 * ```properties
 * super-controller.versioning.enabled=true
 * super-controller.versioning.add-deprecation-headers=false
 * ```
 *
 * Manual configuration (if not using auto-configuration):
 * ```kotlin
 * @Configuration
 * class WebMvcConfig : WebMvcConfigurer {
 *     @Autowired
 *     lateinit var versioningConfig: VersioningConfig
 *
 *     override fun addInterceptors(registry: InterceptorRegistry) {
 *         registry.addInterceptor(ApiVersionInterceptor(versioningConfig))
 *     }
 * }
 * ```
 *
 * @property config The versioning configuration.
 */
class ApiVersionInterceptor(
    private val config: VersioningProperties
) : HandlerInterceptor {

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        if (handler !is HandlerMethod) {
            return true
        }

        val controllerClass = handler.beanType
        val apiVersion = controllerClass.getAnnotation(ApiVersion::class.java)

        apiVersion?.let {
            // Always add version header
            response.addHeader("X-API-Version", it.value)

            // Add deprecation headers if configured and version is deprecated
            if (config.addDeprecationHeaders && it.deprecated) {
                response.addHeader("X-API-Deprecated", "true")

                // Add sunset header if specified
                if (it.sunset.isNotBlank()) {
                    response.addHeader("Sunset", it.sunset)
                    response.addHeader("X-API-Sunset", it.sunset)
                }

                // Add deprecation link if documentation URL is specified
                if (it.documentationUrl.isNotBlank()) {
                    response.addHeader("Link", "<${it.documentationUrl}>; rel=\"deprecation\"")
                }

                // Add warning header (RFC 7234)
                val warningMessage = buildWarningMessage(it)
                response.addHeader("Warning", warningMessage)
            }
        }

        return true
    }

    /**
     * Builds an RFC 7234 compliant warning message.
     *
     * @param apiVersion The API version annotation.
     * @return The warning message.
     */
    private fun buildWarningMessage(apiVersion: ApiVersion): String {
        val message = if (apiVersion.sunset.isNotBlank()) {
            "299 - \"Deprecated API version ${apiVersion.value}. This version will be sunset on ${apiVersion.sunset}.\""
        } else {
            "299 - \"Deprecated API version ${apiVersion.value}. Please migrate to a newer version.\""
        }
        return message
    }
}
