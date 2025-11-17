package io.github.robertomike.super_controller.examples.controllers

import io.github.robertomike.super_controller.controllers.SuperController
import io.github.robertomike.super_controller.examples.models.Order
import io.github.robertomike.super_controller.examples.requests.order.StoreOrderRequest
import io.github.robertomike.super_controller.examples.requests.order.UpdateOrderRequest
import io.github.robertomike.super_controller.examples.services.CachedOrderService
import org.springframework.web.bind.annotation.RestController

@RestController
class CachedOrderController(
    cachedOrderService: CachedOrderService
) : SuperController<Order, Long, StoreOrderRequest, UpdateOrderRequest>(cachedOrderService) {
    override fun setConfig() {
        path = "cached-orders"
        needAuthorization = false
    }
}
