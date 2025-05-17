package io.github.robertomike.super_controller.reactive.examples.requests

import io.github.robertomike.super_controller.requests.Request
import jakarta.validation.constraints.NotNull

open class StoreUserRequest(@field:NotNull val name: String? = null) : Request
