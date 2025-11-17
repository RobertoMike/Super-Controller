package io.github.robertomike.super_controller.repositories

import io.github.robertomike.super_controller.exceptions.SuperControllerException
import io.hypersistence.utils.spring.repository.HibernateRepository
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.Repository
import org.springframework.stereotype.Component
import java.util.*

@Component
@ConditionalOnClass(HibernateRepository::class)
@ConditionalOnProperty("hypersistence-support", prefix = "super-controller", havingValue = "true", matchIfMissing = true)
class HibernateRepositorySupport : RepositorySupport {
    override fun supportIt(repository: Repository<Any, Any>): Boolean {
        return repository is HibernateRepository<*> && repository is JpaRepository
    }

    override fun <M, I> findAll(page: PageRequest, repository: Repository<M, I>): Page<M> {
        return repository.castJpa()
            .findAll(page)
    }

    /**
     * Saves a model to the repository.
     *
     * @param model The model to save.
     */
    override fun <M, I> persist(model: M, repository: Repository<M, I>) {
        repository.castHibernate()
            .persist(model)
    }

    override fun <M, I> findById(
        id: I,
        repository: Repository<M, I>
    ): Optional<M> {
        return repository.castJpa()
            .findById(id as (I & Any))
    }

    /**
     * Saves a model to the repository.
     *
     * @param model The model to save.
     */
    override fun <M, I> update(model: M, repository: Repository<M, I>) {
        repository.castHibernate()
            .update(model)
    }

    /**
     * Saves a model to the repository.
     *
     * @param model The model to save.
     */
    override fun <M, I> delete(model: M, repository: Repository<M, I>) {
        repository.castJpa()
            .delete(model as (M & Any))
    }

    private fun <M, I> Repository<M, I>.castJpa(): JpaRepository<M, I> {
        if (this !is JpaRepository<M, I>) {
            throw SuperControllerException(
                "The repository doesn't extend from JpaRepository"
            )
        }

        return this
    }

    private fun <M, I> Repository<M, I>.castHibernate(): HibernateRepository<M> {
        if (this !is HibernateRepository<*>) {
            throw SuperControllerException(
                "The repository doesn't extend from HibernateRepository"
            )
        }

        return this as HibernateRepository<M>
    }
}