package io.github.robertomike.super_controller.reactive.examples.policies

import io.github.robertomike.super_controller.policies.Policy
import io.github.robertomike.super_controller.reactive.examples.requests.order.StoreOrderRequest
import io.github.robertomike.super_controller.reactive.examples.requests.order.UpdateOrderRequest
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class OrderPolicy : Policy<Long, StoreOrderRequest, UpdateOrderRequest>() {
    override fun viewAll(): Mono<Boolean> {
        return Mono.fromCallable { true }
    }

    override fun store(request: StoreOrderRequest): Mono<Boolean> {
        return Mono.fromCallable { "admin" == request.name }
    }

    override fun view(id: Long): Mono<Boolean> {
        return Mono.fromCallable { id > 1 }
    }

    override fun update(id: Long, request: UpdateOrderRequest): Mono<Boolean> {
        return Mono.fromCallable { "admin" == request.name && id > 1 }
    }

    override fun destroy(id: Long): Mono<Boolean> {
        return Mono.fromCallable { id > 1 }
    }
}