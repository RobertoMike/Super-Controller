package io.github.robertomike.super_controller.config.router

import io.github.robertomike.super_controller.controllers.CrudController
import io.github.robertomike.super_controller.controllers.markers.BulkOperationsMarker
import io.github.robertomike.super_controller.controllers.markers.SoftDeletableMarker
import io.github.robertomike.super_controller.enums.Methods
import org.slf4j.LoggerFactory
import org.springframework.aop.framework.Advised
import org.springframework.aop.support.AopUtils
import org.springframework.web.bind.annotation.RequestMethod
import java.lang.reflect.Method

abstract class BaseRouter<out C : CrudController<*, *, *, *, *, *>>(
    val controllers: List<C>
) {
    private val logger = LoggerFactory.getLogger(BaseRouter::class.java)

    fun registerAll() {
        controllers.map {
            if (AopUtils.isAopProxy(it) && it is Advised) {
                return@map it.targetSource.target as C
            }

            return@map it
        }.forEach { controller ->
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

        // Check if controller implements BulkOperations
        if (controller is BulkOperationsMarker<*, *, *, *>) {
            logger.debug("Registering BulkOperations routes for ${controller::class.simpleName}")

            registerUrl(
                controller,
                "bulkStore",
                "$baseUrl/bulk",
                httpMethod = RequestMethod.POST
            )
            registerUrl(
                controller,
                "bulkUpdate",
                "$baseUrl/bulk",
                httpMethod = RequestMethod.PUT
            )
            registerUrl(
                controller,
                "bulkDelete",
                "$baseUrl/bulk",
                httpMethod = RequestMethod.DELETE
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
                httpMethod = RequestMethod.DELETE
            )

            // PUT /{id}/restore
            registerUrl(
                controller,
                "restore",
                "$baseUrl/{id}/restore",
                httpMethod = RequestMethod.PUT
            )

            // DELETE /{id}/force
            registerUrl(
                controller,
                "forceDelete",
                "$baseUrl/{id}/force",
                httpMethod = RequestMethod.DELETE
            )
        }
    }

    fun registerCrud(controller: @UnsafeVariance C, baseUrl: String, urls: List<Methods>) {
        urls.forEach { url ->
            when (url) {
                Methods.INDEX -> registerUrl(
                    controller,
                    "index", baseUrl, httpMethod = RequestMethod.GET
                )

                Methods.STORE -> registerUrl(
                    controller,
                    "store",
                    baseUrl,
                    httpMethod = RequestMethod.POST
                )

                Methods.SHOW -> registerUrl(controller, "show", "$baseUrl/{id}", RequestMethod.GET)
                Methods.UPDATE -> registerUrl(
                    controller,
                    "update",
                    "$baseUrl/{id}",
                    RequestMethod.PUT
                )

                Methods.DESTROY -> registerUrl(
                    controller,
                    "destroy",
                    "$baseUrl/{id}",
                    RequestMethod.DELETE
                )
            }
        }
    }

    /**
     * Registers a URL mapping for a specific method in the current class.
     *
     * @param method The name of the method to be mapped. Must match the name of a method in the current class.
     * @param url The URL pattern to be mapped to the specified method.
     * @param httpMethod The HTTP method (e.g., GET, POST) for the mapping.
     * @throws io.github.robertomike.super_controller.exceptions.SuperControllerException if the method cannot be registered due to reflection issues or other errors.
     */
    abstract fun registerUrl(
        controller: Any,
        method: String,
        url: String,
        httpMethod: RequestMethod
    )

    fun searchMethodFor(controller: Any, method: String): Method {
        return controller.javaClass.methods.find {
            it.name == method && !it.isBridge
        } ?: throw RuntimeException("Cannot find method $method in $controller")
    }
}