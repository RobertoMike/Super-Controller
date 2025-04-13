package io.github.robertomike.super_controller.examples.controllers

import io.github.robertomike.super_controller.controllers.SuperController
import io.github.robertomike.super_controller.examples.models.Order
import io.github.robertomike.super_controller.examples.policies.OrderPolicy
import io.github.robertomike.super_controller.examples.services.OrderService
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/assets")
class OrderController(service: OrderService, policy: OrderPolicy) : SuperController<Order, Long>(service, policy = policy) {

}
