package io.github.robertomike.super_controller.repositories

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.Repository
import java.util.Optional

interface RepositorySupport {
    fun supportIt(repository: Repository<Any, Any>): Boolean
    fun <M, I> findAll(page: PageRequest, repository: Repository<M, I>): Page<M>
    fun <M, I> persist(model: M, repository: Repository<M, I>)
    fun <M, I> findById(id: I, repository: Repository<M, I>): Optional<M>
    fun <M, I> update(model: M, repository: Repository<M, I>)
    fun <M, I> delete(model: M, repository: Repository<M, I>)
}