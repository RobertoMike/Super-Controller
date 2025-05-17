package io.github.robertomike.super_controller.reactive.examples.controllers

import io.github.robertomike.super_controller.controllers.SuperController
import io.github.robertomike.super_controller.reactive.examples.models.Order
import io.github.robertomike.super_controller.reactive.examples.policies.OrderPolicy
import io.github.robertomike.super_controller.reactive.examples.requests.order.StoreOrderRequest
import io.github.robertomike.super_controller.reactive.examples.requests.order.UpdateOrderRequest
import io.github.robertomike.super_controller.reactive.examples.services.OrderService
import org.springframework.web.bind.annotation.RestController

@RestController
class OrderController(policy: OrderPolicy, service: OrderService) : SuperController<Order, Long, StoreOrderRequest, UpdateOrderRequest>(service) {
    init {
        this.policy = policy
    }
}
