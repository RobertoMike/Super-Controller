package io.github.robertomike.super_controller.examples.controllers

import io.github.robertomike.super_controller.controllers.SuperController
import io.github.robertomike.super_controller.examples.models.Order
import io.github.robertomike.super_controller.examples.policies.OrderPolicy
import org.springframework.web.bind.annotation.RestController

@RestController
class OrderController(policy: OrderPolicy) : SuperController<Order, Long>() {
    init {
        this.policy = policy
    }
}
