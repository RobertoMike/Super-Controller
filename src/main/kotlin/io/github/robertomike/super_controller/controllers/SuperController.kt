package io.github.robertomike.super_controller.controllers

import io.github.robertomike.super_controller.enums.Methods.DESTROY
import io.github.robertomike.super_controller.enums.Methods.INDEX
import io.github.robertomike.super_controller.enums.Methods.SHOW
import io.github.robertomike.super_controller.enums.Methods.STORE
import io.github.robertomike.super_controller.enums.Methods.UPDATE
import io.github.robertomike.super_controller.exceptions.SuperControllerException
import io.github.robertomike.super_controller.policies.Policy
import io.github.robertomike.super_controller.requests.Request
import io.github.robertomike.super_controller.services.BasicService
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * Abstract base class for controllers with superpowers.
 *
 * This class provides a basic implementation for CRUD operations and allows for customization
 * through the use of policies, services and request/response classes.
 *
 * @param M The type of the model being controlled.
 * @param ID The type of the ID used to identify the model.
 */
abstract class SuperController<M, ID : Any>() : BaseController<M, ID, Any, Page<*>>() {
    /**
     * The service used for business logic.
     */
    lateinit var service: BasicService<M, Page<M>, ID, Request, Request>

    @JvmOverloads
    constructor(
        service: BasicService<M, Page<M>, ID, Request, Request>,
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
     * The application context.
     */
    @Autowired
    private lateinit var applicationContext: ApplicationContext


    /**
     * Initializes the controller reading the routes and registering them.
     */
    @PostConstruct
    fun init() {
        setConfig()

        if (::service.isInitialized.not()) {
            service = applicationContext.resolveBeanFor(nameModel + properties.classSuffix.service)
        }

        if (needAuthorization && policy == null) {
            policy = applicationContext.resolveBeanFor(nameModel + properties.classSuffix.policy)
        }

        if (basePackage == null) {
            throw SuperControllerException("The base package is not defined")
        }

        routerUtil.registerCrud(this, baseUrl, urls)
    }

    /**
     * Handles the index action, returning a list of models.
     *
     * @param page The page number for pagination
     * @param size The page size for pagination
     * @return A list of models
     */
    override fun index(
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
    override fun store(@RequestBody json: String): Any {
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
    override fun show(@PathVariable id: ID): Any {
        executePolicy(SHOW, id)
        return transform(service.show(id))
    }

    /**
     * Handles the update action, updating an existing model.
     *
     * @param id The ID of the model to update
     * @param json The JSON data for the updated model
     */
    override fun update(@PathVariable id: ID, @RequestBody json: String): Any {
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
    override fun destroy(@PathVariable id: ID) {
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
}
