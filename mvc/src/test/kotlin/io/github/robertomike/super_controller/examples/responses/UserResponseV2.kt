package io.github.robertomike.super_controller.examples.responses

import io.github.robertomike.super_controller.responses.Response

/**
 * V2 response for User - includes email field that V1 doesn't have.
 * This allows tests to verify they're getting V2 responses vs V1.
 */
data class UserResponseV2(
    var id: Long?,
    var name: String?,
    var email: String?  // V2 includes email field
) : Response {
    constructor(): this(null, null, null)
}
