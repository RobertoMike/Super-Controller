package io.github.robertomike.super_controller.services

import io.github.robertomike.super_controller.exceptions.NotFoundException
import io.github.robertomike.super_controller.exceptions.SuperControllerException
import io.github.robertomike.super_controller.repositories.RepositorySupport
import io.github.robertomike.super_controller.requests.Request
import io.github.robertomike.super_controller.services.interfaces.AfterAndBeforeActions
import io.github.robertomike.super_controller.services.interfaces.BasicService
import io.github.robertomike.super_controller.services.interfaces.MappingActions
import io.github.robertomike.super_controller.utils.ClassUtils
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.Repository
import org.springframework.transaction.annotation.Transactional

/**
 * Abstract base class for services that provides basic CRUD operations.
 *
 * @param M The type of the model being managed by this service.
 * @param ID The type of the ID of the model being managed by this service.
 */
abstract class SuperService<M, ID, SR : Request, UR : Request> : ClassUtils,
    AfterAndBeforeActions<M, Page<M>, SR, UR, Unit>, BasicService<M, Page<M>, ID, SR, UR, Unit>,
    MappingActions<M, SR, UR> {

    @Autowired
    lateinit var repositorySupport: RepositorySupport

    /**
     * Initializes the service by calling the [config] method.
     */
    @PostConstruct
    private fun init() {
        config()
    }

    /**
     * Configures the service. This method can be overridden by subclasses to perform custom configuration.
     */
    open fun config() {
    }

    /**
     * Returns a page of models based on the provided page request.
     *
     * @param page The page request.
     * @return A page of models.
     */
    override fun index(page: PageRequest, params: Map<String, String>): Page<M> {
        val models = repositorySupport.findAll(page, repository)

        afterIndex(models)

        return models
    }

    /**
     * Stores a new model based on the provided request.
     *
     * @param request The request.
     * @return The stored model.
     */
    @Transactional
    override fun store(request: SR): M {
        val model = mappingStore(request)

        beforeStore(model, request)

        repositorySupport.persist(model, repository)

        afterStore(model, request)

        return model
    }

    /**
     * Returns a model based on the provided ID.
     *
     * @param model The model.
     * @return The model.
     */
    override fun show(model: M): M {
        afterShow(model)

        return model
    }

    /**
     * Updates a model based on the provided request.
     *
     * @param model The model being updated.
     * @param request The request.
     * @return The updated model.
     */
    @Transactional
    override fun update(model: M, request: UR): M {
        beforeUpdate(model, request)

        mappingUpdate(request, model)

        repositorySupport.update(model, repository)

        afterUpdate(model, request)

        return model
    }

    /**
     * Deletes a model based on the provided ID.
     *
     * @param model The model being deleted.
     */
    @Transactional
    override fun delete(model: M) {
        beforeDelete(model)

        repositorySupport.delete(model, repository)

        afterDelete(model)
    }

    /**
     * Searches for a model by ID.
     *
     * @param id The ID of the model to search for.
     * @return The model, or throws [NotFoundException] if not found.
     */
    override fun findById(id: ID): M {
        val repository = repository

        val model = repositorySupport.findById(id, repository)

        return model.orElseThrow {
            NotFoundException(
                "Cannot find model with id $id"
            )
        }
    }

    /**
     * The class of the model declared in the generics' controller.
     */
    @Suppress("UNCHECKED_CAST")
    val model: Class<M>
        get() = generics[0] as Class<M>

    /**
     * The service used for business logic.
     */
    open val repository: Repository<M, ID>
        get() {
            throw SuperControllerException("Get repository not implemented")
        }

    override fun defaultAction() {}
}
