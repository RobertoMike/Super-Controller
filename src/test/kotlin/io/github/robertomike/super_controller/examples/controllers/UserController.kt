package io.github.robertomike.super_controller.examples.controllers

import io.github.robertomike.super_controller.controllers.SuperController
import io.github.robertomike.super_controller.examples.mappers.UserResponseMapper
import io.github.robertomike.super_controller.examples.models.User
import io.github.robertomike.super_controller.examples.requests.UserRequest
import io.github.robertomike.super_controller.examples.services.UserService
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/assets")
class UserController(service: UserService, override val mapper: UserResponseMapper) :
    SuperController<User, Long>(service) {
    init {
        needAuthorization = false
        storeRequest = UserRequest::class.java
        updateRequest = UserRequest::class.java
    }
}
