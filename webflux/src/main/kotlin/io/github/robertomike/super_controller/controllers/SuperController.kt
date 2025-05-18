package io.github.robertomike.super_controller.controllers

import io.github.robertomike.super_controller.enums.Methods.*
import io.github.robertomike.super_controller.exceptions.SuperControllerException
import io.github.robertomike.super_controller.exceptions.UnauthorizedException
import io.github.robertomike.super_controller.policies.Policy
import io.github.robertomike.super_controller.requests.Request
import io.github.robertomike.super_controller.services.interfaces.BasicService
import jakarta.annotation.PostConstruct
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import reactor.core.publisher.Mono

abstract class SuperController<M, ID : Any, SR: Request, UR: Request>() :
    CrudController<ID, Mono<Any>, Mono<SR>, Mono<UR>, Mono<Page<*>>, Mono<Unit>>,
    ControllerUtil<M, ID, Mono<Boolean>>() {
    /**
     * The service used for business logic.
     */
    lateinit var service: BasicService<Mono<M>, Mono<Page<M>>, ID, SR, UR, Mono<Unit>>

    @JvmOverloads
    constructor(
        service: BasicService<Mono<M>, Mono<Page<M>>, ID, SR, UR, Mono<Unit>>,
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
    }

    override fun index(
        @RequestParam(value = "page", defaultValue = "0") page: Int,
        @RequestParam(value = "size", defaultValue = "10") size: Int
    ): Mono<Page<*>> {
        return executePolicy(INDEX).policyIsValid()
            .then(service.index(PageRequest.of(page, size)))
            .map { transform(it) }
    }

    override fun store(@Valid @RequestBody monoRequest: Mono<SR>): Mono<Any> {
        return monoRequest.flatMap { request ->
            executePolicy(STORE, request = request).policyIsValid()
                .thenReturn(request)
        }.flatMap { request -> service.store(request) }
            .map { model -> transform(model) }
    }

    override fun show(@PathVariable id: ID): Mono<Any> {
        return executePolicy(SHOW, id).policyIsValid()
            .then(service.show(id))
            .map { model -> transform(model) }
    }

    override fun update(@PathVariable id: ID, @Valid @RequestBody monoRequest: Mono<UR>): Mono<Any> {
        return monoRequest.flatMap { request ->
            executePolicy(UPDATE, id, request).policyIsValid()
                .thenReturn(request)
        }.flatMap { request -> service.update(id, request) }
            .map { model -> transform(model) }
    }

    override fun destroy(@PathVariable id: ID): Mono<Unit> {
        return executePolicy(DESTROY, id).policyIsValid()
            .then(service.delete(id))
    }

    /**
     * Transforms a model into a response.
     *
     * @param model The model to transform
     * @return The transformed response
     */
    open fun transform(model: M): Any {
        mapper?.let { mapper -> return mapper.mapDetail(model) }

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

    override fun noPolicy(): Mono<Boolean> = Mono.just(true)

    private fun Mono<Boolean>.policyIsValid(): Mono<Unit> {
        return this.flatMap {
            return@flatMap if (it.not()) {
                Mono.error(UnauthorizedException("You're not authorized to access this resource"))
            } else {
                Mono.empty()
            }
        }
    }
}