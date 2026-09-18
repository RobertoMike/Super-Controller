package io.github.robertomike.super_controller.repositories

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.Repository
import java.util.Optional

interface RepositorySupport {
    fun supportIt(repository: Repository<Any, Any>): Boolean
    fun <M : Any, I : Any> findAll(page: PageRequest, repository: Repository<M, I>): Page<M>
    fun <M : Any, I : Any> persist(model: M, repository: Repository<M, I>)
    fun <M : Any, I : Any> findById(id: I, repository: Repository<M, I>): Optional<M>
    fun <M : Any, I : Any> update(model: M, repository: Repository<M, I>)
    fun <M : Any, I : Any> delete(model: M, repository: Repository<M, I>)
}