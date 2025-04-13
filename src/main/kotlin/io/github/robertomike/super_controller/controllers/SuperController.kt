package io.github.robertomike.super_controller.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.robertomike.super_controller.config.ConfigProperties
import io.github.robertomike.super_controller.enums.Methods
import io.github.robertomike.super_controller.enums.Methods.DESTROY
import io.github.robertomike.super_controller.enums.Methods.INDEX
import io.github.robertomike.super_controller.enums.Methods.SHOW
import io.github.robertomike.super_controller.enums.Methods.STORE
import io.github.robertomike.super_controller.enums.Methods.UPDATE
import io.github.robertomike.super_controller.exceptions.SuperControllerException
import io.github.robertomike.super_controller.exceptions.UnauthorizedException
import io.github.robertomike.super_controller.mappers.ResponseMapper
import io.github.robertomike.super_controller.policies.Policy
import io.github.robertomike.super_controller.requests.Request
import io.github.robertomike.super_controller.responses.Response
import io.github.robertomike.super_controller.services.BasicService
import io.github.robertomike.super_controller.utils.ControllerUtils
import jakarta.annotation.PostConstruct
import jakarta.validation.Validator
import org.atteo.evo.inflector.English
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.servlet.mvc.method.RequestMappingInfo
import org.springframework.web.servlet.mvc.method.RequestMappingInfo.BuilderConfiguration
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import org.springframework.web.util.pattern.PathPatternParser
import java.util.*

/**
 * Abstract base class for controllers with superpowers.
 *
 * This class provides a basic implementation for CRUD operations and allows for customization
 * through the use of policies, services and request/response classes.
 *
 * @param M The type of the model being controlled.
 * @param ID The type of the ID used to identify the model.
 */
abstract class SuperController<M, ID : Any>() : ControllerUtils() {
    /**
     * The service used for business logic.
     */
    lateinit var service: BasicService<M, ID, Request, Request>

    /**
     * The policy used for authorization.
     */
    var policy: Policy<ID, Request, Request>? = null

    /**
     * Whether authorization is required for this controller.
     */
    var needAuthorization = true

    @JvmOverloads
    constructor(
        service: BasicService<M, ID, Request, Request>,
        needAuthorization: Boolean = true,
        policy: Policy<ID, Request, Request>? = null,
        basePackage: String? = null
    ) : this() {
        this.needAuthorization = needAuthorization
        this.policy = policy
        this.service = service
        this.basePackage = basePackage
    }

    /**
     * The builder configuration for the request mapping handler.
     */
    private lateinit var builderConfiguration: BuilderConfiguration

    /**
     * The request mapping handler mapping.
     */
    @Autowired
    private lateinit var requestMappingHandlerMapping: RequestMappingHandlerMapping

    /**
     * The application context.
     */
    @Autowired
    private lateinit var applicationContext: ApplicationContext

    /**
     * The object mapper used for JSON serialization and deserialization.
     */
    @Autowired
    override lateinit var objectMapper: ObjectMapper

    /**
     * The validator used for validating requests.
     */
    @Autowired
    override lateinit var validator: Validator


    /**
     * The mapper used for to map requests and responses for business logic.
     */
    open val mapper: ResponseMapper<M, out Response, out Response>? = null

    /**
     * The configuration properties for the super controller.
     */
    @Autowired
    override lateinit var properties: ConfigProperties

    /**
     * The base URL for the controller
     */
    open val baseUrl: String
        get() {
            return properties.prefixUrl + pluralNameModel.lowercase(Locale.getDefault())
        }

    /**
     * The base package for the controller to search the other necessary classes.
     */
    override var basePackage: String? = null
        get() {
            return field ?: properties.basePackage
        }

    /**
     * The class of the model declared in the generics' controller.
     */
    @Suppress("UNCHECKED_CAST")
    private val model: Class<M>
        get() = generics[0] as Class<M>

    /**
     * The name of the model
     */
    override val nameModel: String
        get() {
            return model.simpleName
        }

    /**
     * The name of the model in plurar
     */
    private val pluralNameModel: String
        get() {
            return English.plural(nameModel)
        }

    /**
     * Initializes the controller reading the routes and registering them.
     */
    @PostConstruct
    fun init() {
        setConfig()

        service = applicationContext.getBean(
            findClass(
                nameModel + properties.classSuffix.service,
                BasicService::class.java
            ) as Class<BasicService<M, ID, Request, Request>>
        )

        if (needAuthorization) {
            policy = applicationContext.getBean(
                findClass(
                    nameModel + properties.classSuffix.policy,
                    Policy::class.java
                ) as Class<Policy<ID, Request, Request>>
            )
        }

        if (basePackage == null) {
            throw SuperControllerException("The base package is not defined")
        }

        builderConfiguration = BuilderConfiguration()
        builderConfiguration.patternParser = PathPatternParser()

//        log.info(
//            "Registering SuperController for {} model, the base url is {}, with these methods: {}",
//            nameModel), baseUrl(), urls
//        )

        urls.forEach { url ->
            when (url) {
                INDEX -> registerUrl(
                    "index", RequestMethod.GET,
                    Int::class.java,
                    Int::class.java
                )

                STORE -> registerUrl("store", RequestMethod.POST, String::class.java)
                SHOW -> registerUrl("show", "/{id}", RequestMethod.GET, Any::class.java)
                UPDATE -> registerUrl(
                    "update",
                    "/{id}",
                    RequestMethod.PUT,
                    Any::class.java,
                    String::class.java
                )

                DESTROY -> registerUrl("destroy", "/{id}", RequestMethod.DELETE, Any::class.java)
            }
        }
    }

    /**
     * Registers a URL for the given method and HTTP request method.
     *
     * @param method The method to register (e.g. "index", "store", etc.)
     * @param httpMethod The HTTP request method (e.g. RequestMethod.GET, RequestMethod.POST, etc.)
     * @param params The parameter types for the method
     */
    private fun registerUrl(method: String, httpMethod: RequestMethod, vararg params: Class<*>) {
        registerUrl(method, "", httpMethod, *params)
    }

    /**
     * Registers a URL for the given method and HTTP request method, with an additional URL path.
     *
     * @param method The method to register (e.g. "index", "store", etc.)
     * @param additionalUrl The additional URL path
     * @param httpMethod The HTTP request method (e.g. RequestMethod.GET, RequestMethod.POST, etc.)
     * @param params The parameter types for the method
     */
    private fun registerUrl(method: String, additionalUrl: String, httpMethod: RequestMethod, vararg params: Class<*>) {
        val url = baseUrl + additionalUrl
        try {
            val requestMapping = RequestMappingInfo.paths(url)
                .methods(httpMethod).options(builderConfiguration)
                .build()
            val controllerMethod = javaClass.getMethod(method, *params)
            requestMappingHandlerMapping.registerMapping(requestMapping, this, controllerMethod)
        } catch (e: Exception) {
            throw SuperControllerException("Cannot register this method $method", e)
        }
    }

    /**
     * Handles the index action, returning a list of models.
     *
     * @param page The page number for pagination
     * @param size The page size for pagination
     * @return A list of models
     */
    open fun index(
        @RequestParam(value = "page", defaultValue = "0") page: Int,
        @RequestParam(value = "size", defaultValue = "10") size: Int
    ): Page<*> {
        executePolicy(INDEX)
        return transform(
            service.index(PageRequest.of(page, size))
        )
    }

    /**
     * Handles the store action, creating a new model.
     *
     * @param json The JSON data for the new model
     */
    @ResponseStatus(HttpStatus.CREATED)
    open fun store(@RequestBody json: String): Any {
        val request = findRequestForAndMap(STORE, json)
        executePolicy(STORE, request = request)
        return transform(
            service.store(request)
        )
    }

    /**
     * Handles the show action, returning a single model by ID.
     *
     * @param id The ID of the model to retrieve
     * @return The model
     */
    open fun show(@PathVariable id: ID): Any {
        executePolicy(SHOW, id)
        return transform(service.show(id))
    }

    /**
     * Handles the update action, updating an existing model.
     *
     * @param id The ID of the model to update
     * @param json The JSON data for the updated model
     */
    open fun update(@PathVariable id: ID, @RequestBody json: String): Any {
        val request = findRequestForAndMap(UPDATE, json)
        executePolicy(UPDATE, id, request)
        return transform(
            service.update(id, request)
        )
    }

    /**
     * Handles the destroy action, deleting a model by ID.
     *
     * @param id The ID of the model to delete
     */
    open fun destroy(@PathVariable id: ID) {
        executePolicy(DESTROY, id)
        service.delete(id)
    }

    /**
     * Transforms a model into a response.
     *
     * @param model The model to transform
     * @return The transformed response
     */
    open fun transform(model: M): Any {
        mapper?.let { return it.mapDetail(model) }

        return model as Any
    }

    /**
     * Transforms a page of models into a response.
     *
     * @param page The page of models to transform
     * @return The transformed response
     */
    open fun transform(page: Page<M>): Page<*> {
        mapper?.let { return page.map { m -> it.mapList(m) } }

        return page
    }

    /**
     * Executes the policy for the given method and ID.
     *
     * @param method The method to execute (e.g. Methods.INDEX, Methods.STORE, etc.)
     * @param id The ID of the model (optional)
     * @param request The request data (optional)
     */
    private fun executePolicy(method: Methods, id: ID? = null, request: Request? = null) {
        if (!needAuthorization) {
            return
        }

        val policy = policy ?: throw SuperControllerException("Policy not found")

        if (method in listOf(SHOW, UPDATE, DESTROY) && id == null) {
            throw SuperControllerException("Id cannot be null")
        }
        if (method in listOf(STORE, UPDATE) && request == null) {
            throw SuperControllerException("Id cannot be null")
        }

        val authorized: Boolean = when (method) {
            INDEX -> policy.viewAll()
            STORE -> policy.store(request!!)
            SHOW -> policy.view(id!!)
            UPDATE -> policy.update(id!!, request!!)
            DESTROY -> policy.destroy(id!!)
        }

        if (!authorized) {
            throw UnauthorizedException("Unauthorized access to " + method.name + " for " + nameModel)
        }
    }
}
