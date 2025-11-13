package io.github.robertomike.super_controller.examples.services

import io.github.robertomike.super_controller.SuperCache
import io.github.robertomike.super_controller.examples.mappers.OrderMapper
import io.github.robertomike.super_controller.examples.models.Order
import io.github.robertomike.super_controller.examples.repositories.OrderRepository
import io.github.robertomike.super_controller.examples.repositories.UserRepository
import io.github.robertomike.super_controller.examples.requests.order.StoreOrderRequest
import io.github.robertomike.super_controller.examples.requests.order.UpdateOrderRequest
import io.github.robertomike.super_controller.exceptions.NotFoundException
import io.github.robertomike.super_controller.services.SuperService
import org.springframework.stereotype.Service

@Service
@SuperCache(
    value = "orders",
    prefix = "order",
    saveIndex = true,
    saveSingle = true,
    saveOnStore = true
)
open class CachedOrderService(
    override val repository: OrderRepository,
    private val userRepository: UserRepository,
    override val mapper: OrderMapper
) : SuperService<Order, Long, StoreOrderRequest, UpdateOrderRequest>() {
    
    override fun beforeStore(model: Order, request: StoreOrderRequest) {
        val user = userRepository.findById(request.userId!!)
        model.user = user.orElseThrow { NotFoundException("User not found") }
    }
}
