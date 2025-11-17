package io.github.robertomike.super_controller.examples.controllers

import io.github.robertomike.super_controller.controllers.SuperController
import io.github.robertomike.super_controller.examples.models.Order
import io.github.robertomike.super_controller.examples.policies.OrderPolicy
import io.github.robertomike.super_controller.examples.requests.order.StoreOrderRequest
import io.github.robertomike.super_controller.examples.requests.order.UpdateOrderRequest
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.RestController

@RestController
@Profile("!cache-test")
open class OrderController(policy: OrderPolicy) : SuperController<Order, Long, StoreOrderRequest, UpdateOrderRequest>() {
    init {
        this.policy = policy
    }
}
