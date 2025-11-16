package io.github.robertomike.super_controller.config.router

import io.github.robertomike.super_controller.controllers.CrudController
import io.github.robertomike.super_controller.controllers.markers.BulkOperationsMarker
import io.github.robertomike.super_controller.controllers.markers.SoftDeletableMarker
import io.github.robertomike.super_controller.enums.Methods
import io.github.robertomike.super_controller.versioning.ApiVersion
import io.github.robertomike.super_controller.versioning.VersionStrategy
import io.github.robertomike.super_controller.versioning.VersioningProperties
import org.slf4j.LoggerFactory
import org.springframework.aop.framework.Advised
import org.springframework.aop.support.AopUtils
import org.springframework.web.bind.annotation.RequestMethod
import java.lang.reflect.Method

abstract class BaseRouter<out C : CrudController<*, *, *, *, *, *>>(
    val controllers: List<C>,
    private val versioningProperties: VersioningProperties? = null
) {
    private val logger = LoggerFactory.getLogger(BaseRouter::class.java)

    /**
     * Gets the version from controller's @ApiVersion annotation.
     */
    protected fun getApiVersion(controller: Any): ApiVersion? {
        return controller.javaClass.getAnnotation(ApiVersion::class.java)
    }

    /**
     * Calculates headers required for request mapping based on versioning strategy.
     * For HEADER strategy, adds the version header condition.
     * For ACCEPT_HEADER strategy, adds the Accept header pattern.
     */
    protected fun getVersioningHeaders(controller: Any): Array<String> {
        if (versioningProperties?.enabled != true) return emptyArray()
        
        val apiVersion = getApiVersion(controller) ?: return emptyArray()
        
        return when (versioningProperties.strategy) {
            VersionStrategy.HEADER -> {
                arrayOf("${versioningProperties.headerName}=${apiVersion.value}")
            }
            VersionStrategy.ACCEPT_HEADER -> {
                arrayOf("Accept=${versioningProperties.mediaTypePrefix}.${apiVersion.value}+json")
            }
            else -> emptyArray()
        }
    }

    /**
     * Calculates params required for request mapping based on versioning strategy.
     */
    protected fun getVersioningParams(controller: Any): Array<String> {
        if (versioningProperties?.enabled != true) return emptyArray()
        if (versioningProperties.strategy != VersionStrategy.PARAMETER) return emptyArray()
        
        val apiVersion = getApiVersion(controller) ?: return emptyArray()
        return arrayOf("${versioningProperties.paramName}=${apiVersion.value}")
    }

    fun registerAll() {
        controllers.map {
            if (AopUtils.isAopProxy(it) && it is Advised) {
                return@map it.targetSource.target as C
            }

            return@map it
        }.forEach { controller ->
            // Always manually register since SuperController doesn't use @RequestMapping annotations
            // Instead, we add version conditions (headers/params) to differentiate versions
            
            // Register standard CRUD routes
            registerCrud(controller, controller.baseUrl, controller.urls)

            // Register interface extension methods (BulkOperations, SoftDeletable)
            registerInterfaceMethods(controller)
        }
    }

    /**
     * Registers extension methods from marker interfaces (BulkOperations, SoftDeletable).
     * Scans controller for specific method names and registers them with appropriate HTTP methods.
     */
    private fun registerInterfaceMethods(controller: C) {
        val baseUrl = controller.baseUrl
        val headers = getVersioningHeaders(controller)
        val params = getVersioningParams(controller)

        // Check if controller implements BulkOperations
        if (controller is BulkOperationsMarker<*, *, *, *>) {
            logger.debug("Registering BulkOperations routes for ${controller::class.simpleName}")

            registerUrl(
                controller,
                "bulkStore",
                "$baseUrl/bulk",
                httpMethod = RequestMethod.POST,
                headers = headers,
                params = params
            )
            registerUrl(
                controller,
                "bulkUpdate",
                "$baseUrl/bulk",
                httpMethod = RequestMethod.PUT,
                headers = headers,
                params = params
            )
            registerUrl(
                controller,
                "bulkDelete",
                "$baseUrl/bulk",
                httpMethod = RequestMethod.DELETE,
                headers = headers,
                params = params
            )
        }

        // Check if controller implements SoftDeletable
        if (controller is SoftDeletableMarker<*, *>) {
            logger.debug("Registering SoftDeletable routes for ${controller::class.simpleName}")

            // DELETE /{id}/soft-delete
            registerUrl(
                controller,
                "softDelete",
                "$baseUrl/{id}/soft-delete",
                httpMethod = RequestMethod.DELETE,
                headers = headers,
                params = params
            )

            // PUT /{id}/restore
            registerUrl(
                controller,
                "restore",
                "$baseUrl/{id}/restore",
                httpMethod = RequestMethod.PUT,
                headers = headers,
                params = params
            )

            // DELETE /{id}/force
            registerUrl(
                controller,
                "forceDelete",
                "$baseUrl/{id}/force",
                httpMethod = RequestMethod.DELETE,
                headers = headers,
                params = params
            )
        }
    }

    fun registerCrud(controller: @UnsafeVariance C, baseUrl: String, urls: List<Methods>) {
        val headers = getVersioningHeaders(controller)
        val params = getVersioningParams(controller)

        urls.forEach { url ->
            when (url) {
                Methods.INDEX -> {
                    registerUrl(
                        controller,
                        "index",
                        baseUrl,
                        httpMethod = RequestMethod.GET,
                        headers = headers,
                        params = params
                    )
                }

                Methods.STORE -> {
                    registerUrl(
                        controller,
                        "store",
                        baseUrl,
                        httpMethod = RequestMethod.POST,
                        headers = headers,
                        params = params
                    )
                }

                Methods.SHOW -> {
                    registerUrl(
                        controller,
                        "show",
                        "$baseUrl/{id}",
                        RequestMethod.GET,
                        headers = headers,
                        params = params
                    )
                }

                Methods.UPDATE -> {
                    registerUrl(
                        controller,
                        "update",
                        "$baseUrl/{id}",
                        RequestMethod.PUT,
                        headers = headers,
                        params = params
                    )
                }

                Methods.DESTROY -> {
                    registerUrl(
                        controller,
                        "destroy",
                        "$baseUrl/{id}",
                        RequestMethod.DELETE,
                        headers = headers,
                        params = params
                    )
                }
            }
        }
    }

    /**
     * Registers a URL mapping for a specific method in the current class.
     *
     * @param method The name of the method to be mapped. Must match the name of a method in the current class.
     * @param url The URL pattern to be mapped to the specified method.
     * @param httpMethod The HTTP method (e.g., GET, POST) for the mapping.
     * @param headers Optional headers to differentiate mappings (e.g., for versioning via headers)
     * @param params Optional params to differentiate mappings (e.g., for versioning via query params)
     * @throws io.github.robertomike.super_controller.exceptions.SuperControllerException if the method cannot be registered due to reflection issues or other errors.
     */
    abstract fun registerUrl(
        controller: Any,
        method: String,
        url: String,
        httpMethod: RequestMethod,
        headers: Array<String> = emptyArray(),
        params: Array<String> = emptyArray()
    )

    fun searchMethodFor(controller: Any, method: String): Method {
        return controller.javaClass.methods.find {
            it.name == method && !it.isBridge
        } ?: throw RuntimeException("Cannot find method $method in $controller")
    }
}