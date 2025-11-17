package io.github.robertomike.super_controller.examples.controllers

import io.github.robertomike.super_controller.controllers.SuperController
import io.github.robertomike.super_controller.controllers.markers.BulkOperationsMarker
import io.github.robertomike.super_controller.controllers.markers.SoftDeletableMarker
import io.github.robertomike.super_controller.examples.mappers.UserResponseMapper
import io.github.robertomike.super_controller.examples.models.User
import io.github.robertomike.super_controller.examples.requests.StoreUserRequest
import io.github.robertomike.super_controller.examples.requests.UpdateUserRequest
import io.github.robertomike.super_controller.examples.services.UserService
import org.springframework.web.bind.annotation.RestController

@RestController
open class UserController(
    userService: UserService,
    override val mapper: UserResponseMapper
) :
    SuperController<User, Long, StoreUserRequest, UpdateUserRequest>(userService),
    BulkOperationsMarker<User, Long, StoreUserRequest, UpdateUserRequest>,
    SoftDeletableMarker<User, Long> {
    
    init {
        needAuthorization = false
    }

}