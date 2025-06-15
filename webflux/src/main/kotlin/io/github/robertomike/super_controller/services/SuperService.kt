package io.github.robertomike.super_controller.services

import io.github.robertomike.super_controller.exceptions.NotFoundException
import io.github.robertomike.super_controller.exceptions.SuperControllerException
import io.github.robertomike.super_controller.repository.ReactivePaginatedRepository
import io.github.robertomike.super_controller.requests.Request
import io.github.robertomike.super_controller.services.interfaces.AfterAndBeforeActions
import io.github.robertomike.super_controller.services.interfaces.BasicService
import io.github.robertomike.super_controller.services.interfaces.MappingActions
import io.github.robertomike.super_controller.utils.ClassUtils
import jakarta.annotation.PostConstruct
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.Repository
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono

/**
 * Abstract base class for services that provides basic CRUD operations.
 *
 * @param M The type of the model being managed by this service.
 * @param ID The type of the ID of the model being managed by this service.
 */
//abstract class SuperService<M, ID, SR : Request, UR : Request> : ClassUtils,
//    AfterAndBeforeActions<M, List<M>, SR, UR, Mono<Unit>>,
//    BasicService<Mono<M>, Mono<Page<M>>, ID, SR, UR, Mono<Unit>>,
//    MappingActions<M, SR, UR> {
//    /**
//     * Initializes the service by calling the [config] method.
//     */
//    @PostConstruct
//    private fun init() {
//        config()
//    }
//
//    /**
//     * Configures the service. This method can be overridden by subclasses to perform custom configuration.
//     */
//    open fun config() {
//    }
//
//    /**
//     * Returns a page of models based on the provided page request.
//     *
//     * @param page The page request.
//     * @return A page of models.
//     */
//    override fun index(page: PageRequest): Mono<Page<M>> {
//        val repository = repositoryExtendsPagination()
//
//        return Mono.zip(
//            repository.findAllBy(page).collectList(),
//            repository.count()
//        ).flatMap { tuple ->
//            val content = tuple.t1
//            val total = tuple.t2
//
//            afterIndex(content)
//                .thenReturn(
//                    PageImpl(
//                        content,
//                        Pageable.ofSize(page.pageSize)
//                            .withPage(page.pageNumber),
//                        total
//                    )
//                )
//
//        }
//    }
//
//    /**
//     * Stores a new model based on the provided request.
//     *
//     * @param request The request.
//     * @return The stored model.
//     */
//    // TODO !important add transactions!!!!!
//    override fun store(request: SR): Mono<M> {
//        return Mono.fromCallable { mappingStore(request) }
//            .flatMap {
//                beforeStore(it, request).thenReturn(it)
//            }
//            .flatMap {
//                save(it)
//            }
//            .flatMap {
//                afterStore(it, request).thenReturn(it)
//            }
//    }
//
//    /**
//     * Returns a model based on the provided ID.
//     *
//     * @param id The ID of the model.
//     * @return The model.
//     */
//    override fun show(id: ID): Mono<M> {
//        return beforeShow(id)
//            .then(findById(id))
//            .flatMap {
//                afterShow(it)
//                    .thenReturn(it)
//            }
//    }
//
//    /**
//     * Updates a model based on the provided request.
//     *
//     * @param id The ID of the model being updated.
//     * @param request The request.
//     * @return The updated model.
//     */
//    // TODO !important add transactions!!!!!
//    override fun update(id: ID, request: UR): Mono<M> {
//        return findById(id)
//            .flatMap { model ->
//                beforeUpdate(model, request)
//                    .thenReturn(model)
//            }
//            .flatMap { model ->
//                Mono.fromCallable { mappingUpdate(request, model) }
//                    .thenReturn(model)
//            }
//            .flatMap { model ->
//                save(model)
//            }
//            .flatMap { model ->
//                afterUpdate(model, request)
//                    .thenReturn(model)
//            }
//    }
//
//    /**
//     * Deletes a model based on the provided ID.
//     *
//     * @param id The ID of the model being deleted.
//     */
//    // TODO !important add transactions!!!!!
//    override fun delete(id: ID): Mono<Unit> {
//        return beforeDelete(id)
//            .then(findById(id))
//            .flatMap { model ->
//                deleteByModel(model).thenReturn(model)
//            }
//            .flatMap { model ->
//                afterDelete(model)
//            }
//    }
//
//    /**
//     * Saves a model to the repository.
//     *
//     * @param model The model to save.
//     */
//    open fun save(model: M): Mono<M> {
//        return repositoryExtendsCrudRepository()
//            .save(model)
//    }
//
//    /**
//     * Deletes a model from the repository.
//     *
//     * @param model The model to delete.
//     */
//    open fun deleteByModel(model: M): Mono<Void> {
//        return repositoryExtendsCrudRepository()
//            .delete(model)
//    }
//
//    /**
//     * Returns the repository instance, cast to [CrudRepository].
//     *
//     * @throws SuperControllerException if the repository does not extend from [CrudRepository]
//     */
//    private fun repositoryExtendsCrudRepository(): ReactiveCrudRepository<M, ID> {
//        if (repository !is ReactiveCrudRepository<M, ID>) {
//            throw SuperControllerException(
//                "The repository doesn't extend from ReactiveCrudRepository"
//            )
//        }
//
//        return repository as ReactiveCrudRepository<M, ID>
//    }
//
//    /**
//     * Returns the repository instance, cast to [PagingAndSortingRepository].
//     *
//     * @throws SuperControllerException if the repository does not extend from [PagingAndSortingRepository]
//     */
//    private fun repositoryExtendsPagination(): ReactivePaginatedRepository<M, ID> {
//        if (repository !is ReactivePaginatedRepository<*, *>) {
//            throw SuperControllerException(
//                "The repository doesn't ReactivePaginatedRepository, you should add this interface or customize the index method"
//            )
//        }
//
//        return repository as ReactivePaginatedRepository<M, ID>
//    }
//
//    /**
//     * Searches for a model by ID.
//     *
//     * @param id The ID of the model to search for.
//     * @return The model, or throws [NotFoundException] if not found.
//     */
//    override fun findById(id: ID): Mono<M> {
//        val repository = repository
//
//        val model = when {
//            repository is ReactiveCrudRepository<M, ID> -> repository.findById(id)
//            else -> throw SuperControllerException(
//                "The repository doesn't extend from CrudRepository and customFindById is not active"
//            )
//        }
//
//        return model.switchIfEmpty(
//            Mono.error(
//                NotFoundException(
//                    "Cannot find model with id $id"
//                )
//            )
//        )
//    }
//
//    /**
//     * The class of the model declared in the generics' controller.
//     */
//    @Suppress("UNCHECKED_CAST")
//    val model: Class<M>
//        get() = generics[0] as Class<M>
//
//    /**
//     * The service used for business logic.
//     */
//    open val repository: Repository<M, ID>
//        get() {
//            throw SuperControllerException("Get repository not implemented")
//        }
//
//    override fun defaultAction(): Mono<Unit> = Mono.empty()
//}
