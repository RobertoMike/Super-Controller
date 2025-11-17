package io.github.robertomike.super_controller.config.router

import io.github.robertomike.super_controller.controllers.SuperController
import io.github.robertomike.super_controller.exceptions.SuperControllerException
import io.github.robertomike.super_controller.versioning.VersioningProperties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Configuration
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.servlet.mvc.method.RequestMappingInfo
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping

@Configuration
@ConditionalOnClass(name = ["org.springframework.web.servlet.config.annotation.WebMvcConfigurer"])
open class RouterConfig(
    @Qualifier("requestMappingHandlerMapping")
    val mapper: RequestMappingHandlerMapping,
    controllers: List<SuperController<*, *, *, *>>,
    @Autowired(required = false)
    versioningProperties: VersioningProperties?
) : BaseRouter<SuperController<*, *, *, *>>(controllers, versioningProperties) {
    /**
     * The builder configuration for the request mapping handler.
     */
    private val builderConfiguration = mapper.builderConfiguration

    init {
        registerAll()
    }

    /**
     * Registers a URL mapping for a specific method in the current class.
     *
     * @param method The name of the method to be mapped. Must match the name of a method in the current class.
     * @param url The URL pattern to be mapped to the specified method.
     * @param httpMethod The HTTP method (e.g., GET, POST) for the mapping.
     * @param headers Optional headers to differentiate mappings (e.g., for versioning via headers)
     * @param params Optional params to differentiate mappings (e.g., for versioning via query params)
     * @throws SuperControllerException if the method cannot be registered due to reflection issues or other errors.
     */
    override fun registerUrl(
        controller: Any,
        method: String,
        url: String,
        httpMethod: RequestMethod,
        headers: Array<String>,
        params: Array<String>
    ) {
        try {
            val requestMapping = RequestMappingInfo.paths(url)
                .methods(httpMethod)
                .headers(*headers)
                .params(*params)
                .options(builderConfiguration)
                .build()
            val controllerMethod = searchMethodFor(controller, method)

            mapper.registerMapping(requestMapping, controller, controllerMethod)
        } catch (e: Exception) {
            throw SuperControllerException("Cannot register this method $method", e)
        }
    }
}