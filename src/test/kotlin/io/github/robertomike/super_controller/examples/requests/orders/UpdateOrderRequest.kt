package io.github.robertomike.super_controller.examples.requests.orders

import io.github.robertomike.super_controller.requests.Request
import jakarta.validation.constraints.NotNull

data class UpdateOrderRequest(
    @field:NotNull
    val name: String? = null,
    @field:NotNull
    val price: Double? = null
) : Request