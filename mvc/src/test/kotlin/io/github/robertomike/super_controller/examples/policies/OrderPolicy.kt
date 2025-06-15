package io.github.robertomike.super_controller.examples.policies

import io.github.robertomike.super_controller.examples.models.Order
import io.github.robertomike.super_controller.examples.requests.order.StoreOrderRequest
import io.github.robertomike.super_controller.examples.requests.order.UpdateOrderRequest
import io.github.robertomike.super_controller.policies.Policy
import org.springframework.stereotype.Component

@Component
class OrderPolicy : Policy<Order, StoreOrderRequest, UpdateOrderRequest>() {
    override fun viewAll(): Boolean {
        return true
    }

    override fun store(request: StoreOrderRequest): Boolean {
        return "admin" == request.name
    }

    override fun view(model: Order): Boolean {
        return model.id!! > 1
    }

    override fun update(model: Order, request: UpdateOrderRequest): Boolean {
        return "admin" == request.name && model.id!! > 1
    }

    override fun destroy(model: Order): Boolean {
        return model.id!! > 1
    }
}
