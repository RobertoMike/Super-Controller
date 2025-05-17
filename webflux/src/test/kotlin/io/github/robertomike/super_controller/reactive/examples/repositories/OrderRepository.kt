package io.github.robertomike.super_controller.reactive.examples.repositories

import io.github.robertomike.super_controller.reactive.examples.models.Order
import io.github.robertomike.super_controller.repository.ReactivePaginatedRepository
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface OrderRepository : ReactivePaginatedRepository<Order, Long>, ReactiveCrudRepository<Order, Long> {
    fun findAllByUserId(userId: Long): Flux<Order>
    fun findOrderById(id: Long): Mono<Order>
}
