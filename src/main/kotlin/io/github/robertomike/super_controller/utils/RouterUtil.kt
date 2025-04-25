package io.github.robertomike.super_controller.utils

import io.github.robertomike.super_controller.enums.Methods
import io.github.robertomike.super_controller.enums.Methods.DESTROY
import io.github.robertomike.super_controller.enums.Methods.INDEX
import io.github.robertomike.super_controller.enums.Methods.SHOW
import io.github.robertomike.super_controller.enums.Methods.STORE
import io.github.robertomike.super_controller.enums.Methods.UPDATE
import io.github.robertomike.super_controller.exceptions.SuperControllerException
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.servlet.mvc.method.RequestMappingInfo
import org.springframework.web.servlet.mvc.method.RequestMappingInfo.BuilderConfiguration
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import org.springframework.web.util.pattern.PathPatternParser

@Component
class RouterUtil(var mapper: RequestMappingHandlerMapping) {
    /**
     * The builder configuration for the request mapping handler.
     */
    private val builderConfiguration = BuilderConfiguration()

    init {
        builderConfiguration.patternParser = PathPatternParser()
    }

    fun registerCrud(controller: Any, baseUrl: String, urls: List<Methods>) {
        urls.forEach { url ->
            when (url) {
                INDEX -> registerUrl(
                    controller,
                    "index", baseUrl, httpMethod = RequestMethod.GET, params = arrayOf(
                        Int::class.java,
                        Int::class.java
                    )
                )

                STORE -> registerUrl(
                    controller,
                    "store",
                    baseUrl,
                    httpMethod = RequestMethod.POST,
                    params = arrayOf(String::class.java)
                )

                SHOW -> registerUrl(controller, "show", "$baseUrl/{id}", RequestMethod.GET, Any::class.java)
                UPDATE -> registerUrl(
                    controller,
                    "update",
                    "/{id}",
                    RequestMethod.PUT,
                    Any::class.java,
                    String::class.java
                )

                DESTROY -> registerUrl(controller, "destroy", "$baseUrl/{id}", RequestMethod.DELETE, Any::class.java)
            }
        }
    }

    /**
     * Registers a URL mapping for a specific method in the current class.
     *
     * @param method The name of the method to be mapped. Must match the name of a method in the current class.
     * @param url The URL pattern to be mapped to the specified method.
     * @param httpMethod The HTTP method (e.g., GET, POST) for the mapping.
     * @param params The parameter types of the method to be registered.
     * @throws SuperControllerException if the method cannot be registered due to reflection issues or other errors.
     */
    fun registerUrl(
        controller: Any,
        method: String,
        url: String,
        httpMethod: RequestMethod,
        vararg params: Class<*>
    ) {
        try {
            val requestMapping = RequestMappingInfo.paths(url)
                .methods(httpMethod)
                .options(builderConfiguration)
                .build()
            val controllerMethod = controller.javaClass
                .getMethod(method, *params)

            mapper.registerMapping(requestMapping, controller, controllerMethod)
        } catch (e: Exception) {
            throw SuperControllerException("Cannot register this method $method", e)
        }
    }
}