package io.github.robertomike.super_controller.repository

import org.springframework.data.domain.PageRequest
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface ReactivePaginatedRepository<M, ID> {
    fun findAllBy(page: PageRequest): Flux<M>
    fun count(): Mono<Long>
}