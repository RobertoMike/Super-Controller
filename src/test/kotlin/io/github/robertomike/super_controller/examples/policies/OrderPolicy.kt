package io.github.robertomike.super_controller.examples.policies

import io.github.robertomike.super_controller.examples.requests.orders.StoreOrderRequest
import io.github.robertomike.super_controller.examples.requests.orders.UpdateOrderRequest
import io.github.robertomike.super_controller.policies.Policy
import io.github.robertomike.super_controller.requests.Request
import org.springframework.stereotype.Component

@Component
class OrderPolicy : Policy<Long>() {
    override fun viewAll(): Boolean {
        return true
    }

    override fun store(request: Request): Boolean {
        val orderRequest: StoreOrderRequest = request as StoreOrderRequest
        return "admin" == orderRequest.name
    }

    override fun view(id: Long): Boolean {
        return id > 1
    }

    override fun update(id: Long, request: Request): Boolean {
        val orderRequest: UpdateOrderRequest = request as UpdateOrderRequest
        return "admin" == orderRequest.name && id > 1
    }

    override fun destroy(id: Long): Boolean {
        return id > 1
    }
}
