package io.github.robertomike.super_controller.examples.requests

import io.github.robertomike.super_controller.requests.Request
import jakarta.validation.constraints.NotNull

class UserRequest(@field:NotNull val name: String? = null) : Request
