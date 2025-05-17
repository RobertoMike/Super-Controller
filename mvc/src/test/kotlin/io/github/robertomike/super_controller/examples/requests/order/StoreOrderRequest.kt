package io.github.robertomike.super_controller.examples.requests.order

import io.github.robertomike.super_controller.requests.Request
import jakarta.validation.constraints.NotNull

data class StoreOrderRequest(
    @field:NotNull
    var name: String? = null,
    @field:NotNull
    var userId: Long? = null,
    @field:NotNull
    var price: Double? = null
) : Request