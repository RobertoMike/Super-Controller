package io.github.robertomike.super_controller.config.router

import io.github.robertomike.super_controller.controllers.CrudController
import io.github.robertomike.super_controller.enums.Methods
import jakarta.annotation.PostConstruct
import org.springframework.web.bind.annotation.RequestMethod
import java.lang.reflect.Method

abstract class BaseRouter<out C : CrudController<*, *, *, *, *, *>>(
    val controllers: List<C>
) {
    @PostConstruct
    fun init() {
        controllers.forEach {
            registerCrud(it, it.baseUrl, it.urls)
        }
    }

    fun registerCrud(controller: Any, baseUrl: String, urls: List<Methods>) {
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
            it.name == method
        } ?: throw RuntimeException("Cannot find method $method in $controller")
    }
}