package io.github.robertomike.super_controller.reactive.examples.services

import io.github.robertomike.super_controller.reactive.examples.mappers.OrderMapper
import io.github.robertomike.super_controller.reactive.examples.models.Order
import io.github.robertomike.super_controller.reactive.examples.repositories.OrderRepository
import io.github.robertomike.super_controller.reactive.examples.repositories.UserRepository
import io.github.robertomike.super_controller.reactive.examples.requests.order.StoreOrderRequest
import io.github.robertomike.super_controller.reactive.examples.requests.order.UpdateOrderRequest
import io.github.robertomike.super_controller.services.SuperService
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
open class OrderService(
    override val repository: OrderRepository,
    private val userRepository: UserRepository,
    override val mapper: OrderMapper
) : SuperService<Order, Long, StoreOrderRequest, UpdateOrderRequest>() {
    override fun beforeStore(model: Order, request: StoreOrderRequest): Mono<Unit> {
        return Mono.fromCallable { model.userId = request.userId!! }
    }
}