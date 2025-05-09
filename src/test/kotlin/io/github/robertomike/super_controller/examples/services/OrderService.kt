package io.github.robertomike.super_controller.examples.services

import io.github.robertomike.baradum.filters.ExactFilter
import io.github.robertomike.baradum.filters.Filter
import io.github.robertomike.baradum.filters.IntervalFilter
import io.github.robertomike.hefesto.actions.JoinFetch
import io.github.robertomike.super_controller.examples.mappers.OrderMapper
import io.github.robertomike.super_controller.examples.models.Order
import io.github.robertomike.super_controller.examples.repositories.OrderRepository
import io.github.robertomike.super_controller.examples.repositories.UserRepository
import io.github.robertomike.super_controller.examples.requests.order.StoreOrderRequest
import io.github.robertomike.super_controller.examples.requests.order.UpdateOrderRequest
import io.github.robertomike.super_controller.exceptions.NotFoundException
import io.github.robertomike.super_controller.services.SuperServiceWithFilters
import org.springframework.stereotype.Service

@Service
class OrderService(
    override val repository: OrderRepository,
    private val userRepository: UserRepository,
    override val mapper: OrderMapper
) : SuperServiceWithFilters<Order, Long, StoreOrderRequest, UpdateOrderRequest>() {
    override fun filters(): List<Filter<*>> {
        return listOf(
            IntervalFilter("price"),
            ExactFilter("userId", "user.id")
        )
    }

    override fun with(): List<JoinFetch> {
        return listOf(JoinFetch.make("user"))
    }

    override fun beforeStore(model: Order, request: StoreOrderRequest) {
        val user = userRepository.findById(request.userId!!)
        model.user = user.orElseThrow { NotFoundException("User not found") }
    }
}