package io.github.robertomike.super_controller.examples.policies

import io.github.robertomike.super_controller.examples.requests.order.StoreRequest
import io.github.robertomike.super_controller.examples.requests.order.UpdateRequest
import io.github.robertomike.super_controller.policies.Policy
import org.springframework.stereotype.Component

@Component
class OrderPolicy : Policy<Long, StoreRequest, UpdateRequest>() {
    override fun viewAll(): Boolean {
        return true
    }

    override fun store(request: StoreRequest): Boolean {
        return "admin" == request.name
    }

    override fun view(id: Long): Boolean {
        return id > 1
    }

    override fun update(id: Long, request: UpdateRequest): Boolean {
        return "admin" == request.name && id > 1
    }

    override fun destroy(id: Long): Boolean {
        return id > 1
    }
}
