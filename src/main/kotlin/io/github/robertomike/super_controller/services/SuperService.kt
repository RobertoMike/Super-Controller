package io.github.robertomike.super_controller.services

import io.github.robertomike.super_controller.exceptions.NotFoundException
import io.github.robertomike.super_controller.exceptions.SuperControllerException
import io.github.robertomike.super_controller.mappers.RequestMapper
import io.github.robertomike.super_controller.requests.Request
import io.github.robertomike.super_controller.utils.ClassUtils
import jakarta.annotation.PostConstruct
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.Repository

/**
 * Abstract base class for services that provides basic CRUD operations.
 *
 * @param M The type of the model being managed by this service.
 * @param ID The type of the ID of the model being managed by this service.
 */
abstract class SuperService<M, ID, SR : Request, UR : Request> : ClassUtils, BasicService<M, ID, SR, UR> {
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
    override fun index(page: PageRequest): Page<M> {
        val models = repositoryExtendsPagination()
            .findAll(page)

        afterIndex(models)

        return models
    }

    /**
     * Called after the index operation. This method can be overridden by subclasses to perform custom logic.
     *
     * @param models The page of models.
     */
    open fun afterIndex(models: Page<M>?) {
    }

    /**
     * Called before the store operation. This method can be overridden by subclasses to perform custom logic.
     *
     * @param model The model being stored.
     * @param request The request.
     */
    open fun beforeStore(model: M, request: SR) {
    }

    /**
     * Stores a new model based on the provided request.
     *
     * @param request The request.
     * @return The stored model.
     */
    override fun store(request: SR): M {
        val model = mappingStore(request)

        beforeStore(model, request)

        save(model)

        afterStore(model, request)

        return model
    }

    /**
     * Called after the store operation. This method can be overridden by subclasses to perform custom logic.
     *
     * @param model The stored model.
     * @param request The request.
     */
    open fun afterStore(model: M, request: SR) {
    }

    /**
     * Called before the show operation. This method can be overridden by subclasses to perform custom logic.
     *
     * @param id The ID of the model being shown.
     */
    open fun beforeShow(id: ID) {
    }

    /**
     * Returns a model based on the provided ID.
     *
     * @param id The ID of the model.
     * @return The model.
     */
    override fun show(id: ID): M {
        beforeShow(id)

        val model = findById(id)

        afterShow(model)

        return model
    }

    /**
     * Called after the show operation. This method can be overridden by subclasses to perform custom logic.
     *
     * @param model The model being shown.
     */
    open fun afterShow(model: M) {
    }

    /**
     * Called before the update operation. This method can be overridden by subclasses to perform custom logic.
     *
     * @param model The model being updated.
     * @param request The request.
     */
    open fun beforeUpdate(model: M, request: UR) {
    }

    /**
     * Updates a model based on the provided request.
     *
     * @param id The ID of the model being updated.
     * @param request The request.
     * @return The updated model.
     */
    override fun update(id: ID, request: UR): M {
        val model = findById(id)

        beforeUpdate(model, request)

        mappingUpdate(request, model)

        save(model)

        afterUpdate(model, request)

        return model
    }

    /**
     * Called after the update operation. This method can be overridden by subclasses to perform custom logic.
     *
     * @param model The updated model.
     * @param request The request.
     */
    open fun afterUpdate(model: M, request: UR) {
    }

    /**
     * Maps the request to the model for store operation.
     *
     * @param request The request.
     */
    open fun mappingStore(request: SR): M {
        return mapper.map(request)
    }

    /**
     * Maps the request to the model for update operation.
     *
     * @param request The request.
     */
    open fun mappingUpdate(request: UR, target: M) {
        mapper.update(request, target)
    }

    /**
     * Called before the delete operation. This method can be overridden by subclasses to perform custom logic.
     *
     * @param id The ID of the model being deleted.
     */
    open fun beforeDelete(id: ID) {
    }

    /**
     * Deletes a model based on the provided ID.
     *
     * @param id The ID of the model being deleted.
     */
    override fun delete(id: ID) {
        beforeDelete(id)

        val model = findById(id)

        deleteByModel(model)

        afterDelete(model)
    }

    /**
     * Called after the delete operation. This method can be overridden by subclasses to perform custom logic.
     *
     * @param model The deleted model.
     */
    open fun afterDelete(model: M) {
    }

    /**
     * Saves a model to the repository.
     *
     * @param model The model to save.
     */
    open fun save(model: M) {
        repositoryExtendsCrudRepository()
            .save(model)
    }

    /**
     * Deletes a model from the repository.
     *
     * @param model The model to delete.
     */
    open fun deleteByModel(model: M) {
        repositoryExtendsCrudRepository()
            .delete(model)
    }

    /**
     * Returns the repository instance, cast to [CrudRepository].
     *
     * @throws SuperControllerException if the repository does not extend from [CrudRepository]
     */
    private fun repositoryExtendsCrudRepository(): CrudRepository<M, ID> {
        if (repository !is CrudRepository<M, ID>) {
            throw SuperControllerException(
                "The repository doesn't extend from CrudRepository"
            )
        }

        return repository as CrudRepository<M, ID>
    }

    /**
     * Returns the repository instance, cast to [PagingAndSortingRepository].
     *
     * @throws SuperControllerException if the repository does not extend from [PagingAndSortingRepository]
     */
    private fun repositoryExtendsPagination(): PagingAndSortingRepository<M, ID> {
        if (repository !is PagingAndSortingRepository<M, ID>) {
            throw SuperControllerException(
                "The repository doesn't extend from PagingAndSortingRepository"
            )
        }

        return repository as PagingAndSortingRepository<M, ID>
    }

    /**
     * Searches for a model by ID.
     *
     * @param id The ID of the model to search for.
     * @return The model, or throws [NotFoundException] if not found.
     */
    open fun findById(id: ID): M {
        val repository = repository

        val model = when {
            repository is CrudRepository<M, ID> -> repository.findById(id)
            else -> throw SuperControllerException(
                "The repository doesn't extend from CrudRepository and customFindById is not active"
            )
        }

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

    /**
     * The mapper used for to map requests and responses for business logic.
     */
    open val mapper: RequestMapper<M, SR, UR>
        get() {
            throw SuperControllerException("Mapper not implemented")
        }
}
