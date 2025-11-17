package io.github.robertomike.super_controller.examples.requests

class UpdateUserRequest(
    name: String? = null,
    email: String? = null
) : StoreUserRequest(name, email)
