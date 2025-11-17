package io.github.robertomike.super_controller.examples.repositories

import io.github.robertomike.super_controller.examples.models.Order
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OrderRepository : JpaRepository<Order, Long> {
    fun findAllByUserId(userId: Long): List<Order>
}
