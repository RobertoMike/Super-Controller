package io.github.robertomike.super_controller.examples.responses

import io.github.robertomike.super_controller.responses.Response

data class UserResponse(
    var id: Long?,
    var name: String?
) : Response {
    constructor(): this(null, null)
}