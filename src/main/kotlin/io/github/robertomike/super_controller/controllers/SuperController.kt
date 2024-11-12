package io.github.robertomike.super_controller.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.robertomike.hefesto.models.BaseModel
import io.github.robertomike.super_controller.config.ConfigProperties
import io.github.robertomike.super_controller.enums.Methods
import io.github.robertomike.super_controller.enums.Methods.*
import io.github.robertomike.super_controller.exceptions.SuperControllerException
import io.github.robertomike.super_controller.exceptions.UnauthorizedException
import io.github.robertomike.super_controller.policies.Policy
import io.github.robertomike.super_controller.requests.Request
import io.github.robertomike.super_controller.services.BasicService
import io.github.robertomike.super_controller.utils.ControllerUtils
import jakarta.annotation.PostConstruct
import jakarta.validation.Validator
import org.atteo.evo.inflector.English
import org.modelmapper.ModelMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.RequestMappingInfo
import org.springframework.web.servlet.mvc.method.RequestMappingInfo.BuilderConfiguration
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import org.springframework.web.util.pattern.PathPatternParser
import java.util.*

abstract class SuperController<M : BaseModel, ID : Any> : ControllerUtils() {
    private lateinit var builderConfiguration: BuilderConfiguration

    @Autowired
    private lateinit var requestMappingHandlerMapping: RequestMappingHandlerMapping

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @Autowired
    override lateinit var mapper: ObjectMapper

    @Autowired
    override lateinit var validator: Validator

    @Autowired
    lateinit var modelMapper: ModelMapper

    @Autowired
    override lateinit var properties: ConfigProperties

    var needAuthorization = true

    private var policy: Policy<ID>? = null
        get() {
            if (field != null) {
                return field
            }

            try {
                field = applicationContext.getBean(
                    findClass(
                        nameModel + properties.classSuffix.policy,
                        Policy::class.java
                    ) as Class<Policy<ID>>
                )

                return field
            } catch (e: Exception) {
                throw SuperControllerException(
                    "Policy for $nameModel not found, created or disable authorization", e
                )
            }
        }

    private var service: BasicService<M, ID>? = null
        get() {
            if (field == null) {
                field = applicationContext.getBean(
                    findClass(
                        nameModel + properties.classSuffix.service,
                        BasicService::class.java
                    ) as Class<BasicService<M, ID>>
                )
            }

            return field
        }

    private val baseUrl: String
        get() {
            return properties.prefixUrl + pluralNameModel.lowercase(Locale.getDefault())
        }

    override var basePackage: String? = null
        get() {
            return field ?: properties.basePackage
        }

    @Suppress("UNCHECKED_CAST")
    private val model: Class<M>
        get() = generics[0] as Class<M>

    override val nameModel: String
        get() {
            return model.simpleName
        }

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

        if (basePackage == null) {
            throw SuperControllerException("The base package is not defined")
        }

        builderConfiguration = BuilderConfiguration()
        builderConfiguration.patternParser = PathPatternParser()

//        SuperController.log.info(
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

    private fun registerUrl(method: String, httpMethod: RequestMethod, vararg params: Class<*>) {
        registerUrl(method, "", httpMethod, *params)
    }

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

    open fun index(
        @RequestParam(value = "page", defaultValue = "0") page: Int,
        @RequestParam(value = "size", defaultValue = "10") size: Int
    ): Page<*> {
        executePolicy(INDEX)
        return transform(
            service!!.index(PageRequest.of(page, size))
        )
    }

    @ResponseStatus(HttpStatus.CREATED)
    open fun store(@RequestBody json: String): Any {
        val request: Request = findRequestFor(STORE, json)
        executePolicy(STORE, request = request)
        return transform(
            service!!.store(request)
        )
    }

    open fun show(@PathVariable() id: ID): Any {
        executePolicy(SHOW, id)
        return transform(service!!.show(id))
    }

    open fun update(@PathVariable() id: ID, @RequestBody json: String): Any {
        val request: Request = findRequestFor(UPDATE, json)
        executePolicy(UPDATE, id, request)
        return transform(
            service!!.update(id, request)
        )
    }

    open fun destroy(@PathVariable() id: ID) {
        executePolicy(DESTROY, id)
        service!!.delete(id)
    }

    protected open fun transform(model: M): Any {
        findResponseFor(SHOW)?.let { return modelMapper.map(model, it) }

        return model
    }

    protected open fun transform(page: Page<M>): Page<*> {
        findResponseFor(INDEX)?.let { return page.map { m -> modelMapper.map(m, it) } }

        return page
    }

    private fun executePolicy(method: Methods, id: ID? = null, request: Request? = null) {
        if (!needAuthorization) {
            return
        }

        val policy: Policy<ID> = policy ?: throw SuperControllerException("Policy not found")

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
