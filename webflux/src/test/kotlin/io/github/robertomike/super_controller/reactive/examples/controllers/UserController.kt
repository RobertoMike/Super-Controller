package io.github.robertomike.super_controller.reactive.examples.controllers

import io.github.robertomike.super_controller.controllers.SuperController
import io.github.robertomike.super_controller.reactive.examples.mappers.UserResponseMapper
import io.github.robertomike.super_controller.reactive.examples.models.User
import io.github.robertomike.super_controller.reactive.examples.requests.StoreUserRequest
import io.github.robertomike.super_controller.reactive.examples.requests.UpdateUserRequest
import io.github.robertomike.super_controller.reactive.examples.services.UserService
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(service: UserService, override val mapper: UserResponseMapper) :
    SuperController<User, Long, StoreUserRequest, UpdateUserRequest>(service) {
    init {
        needAuthorization = false
    }
}
