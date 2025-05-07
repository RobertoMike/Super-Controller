package io.github.robertomike.super_controller.examples.policies

import io.github.robertomike.super_controller.examples.requests.order.StoreOrderRequest
import io.github.robertomike.super_controller.examples.requests.order.UpdateOrderRequest
import io.github.robertomike.super_controller.policies.Policy
import org.springframework.stereotype.Component

@Component
class OrderPolicy : Policy<Long, StoreOrderRequest, UpdateOrderRequest>() {
    override fun viewAll(): Boolean {
        return true
    }

    override fun store(request: StoreOrderRequest): Boolean {
        return "admin" == request.name
    }

    override fun view(id: Long): Boolean {
        return id > 1
    }

    override fun update(id: Long, request: UpdateOrderRequest): Boolean {
        return "admin" == request.name && id > 1
    }

    override fun destroy(id: Long): Boolean {
        return id > 1
    }
}
