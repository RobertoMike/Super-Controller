package io.github.robertomike.super_controller.repositories

import io.github.robertomike.super_controller.exceptions.SuperControllerException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.Repository
import java.util.*

class RepositoryNormalSupport : RepositorySupport {
    override fun supportIt(repository: Repository<Any, Any>): Boolean {
        return repository is CrudRepository && repository is PagingAndSortingRepository<*, *>
    }

    override fun <M, I> findAll(page: PageRequest, repository: Repository<M, I>): Page<M> {
        return repository.extendsPagination()
            .findAll(page)
    }

    /**
     * Saves a model to the repository.
     *
     * @param model The model to save.
     */
    override fun <M, I> persist(model: M, repository: Repository<M, I>) {
        repository.extendsCrudRepository()
            .save(model as (M & Any))
    }

    override fun <M, I> findById(
        id: I,
        repository: Repository<M, I>
    ): Optional<M> {
        return repository.extendsCrudRepository()
            .findById(id as (I & Any))
    }

    /**
     * Saves a model to the repository.
     *
     * @param model The model to save.
     */
    override fun <M, I> update(model: M, repository: Repository<M, I>) {
        repository.extendsCrudRepository()
            .save(model as (M & Any))
    }

    /**
     * Saves a model to the repository.
     *
     * @param model The model to save.
     */
    override fun <M, I> delete(model: M, repository: Repository<M, I>) {
        repository.extendsCrudRepository()
            .delete(model as (M & Any))
    }

    /**
     * Returns the repository instance, cast to [CrudRepository].
     *
     * @throws SuperControllerException if the repository does not extend from [CrudRepository]
     */
    private fun <M, I> Repository<M, I>.extendsCrudRepository(): CrudRepository<M, I> {
        if (this !is CrudRepository<M, I>) {
            throw SuperControllerException(
                "The repository doesn't extend from CrudRepository"
            )
        }

        return this
    }


    /**
     * Returns the repository instance, cast to [PagingAndSortingRepository].
     *
     * @throws SuperControllerException if the repository does not extend from [PagingAndSortingRepository]
     */
    private fun <M, I> Repository<M, I>.extendsPagination(): PagingAndSortingRepository<M, I> {
        if (this !is PagingAndSortingRepository<M, I>) {
            throw SuperControllerException(
                "The repository doesn't extend from PagingAndSortingRepository"
            )
        }

        return this
    }
}