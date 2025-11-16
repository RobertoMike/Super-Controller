package io.github.robertomike.super_controller.examples.controllers

import io.github.robertomike.super_controller.controllers.SuperController
import io.github.robertomike.super_controller.controllers.markers.BulkOperationsMarker
import io.github.robertomike.super_controller.controllers.markers.SoftDeletableMarker
import io.github.robertomike.super_controller.examples.mappers.UserResponseMapperV2
import io.github.robertomike.super_controller.examples.models.User
import io.github.robertomike.super_controller.examples.requests.StoreUserRequest
import io.github.robertomike.super_controller.examples.requests.UpdateUserRequest
import io.github.robertomike.super_controller.examples.services.UserService
import io.github.robertomike.super_controller.versioning.ApiVersion
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.web.bind.annotation.RestController

/**
 * V2 version of User controller, used only when versioning is enabled.
 * This controller is used in versioning integration tests.
 * 
 * Uses the same path as UserController but is differentiated by version headers/params.
 * Returns UserResponseV2 which includes email field (V1 doesn't have this).
 */
@RestController
@ApiVersion("V2")
@ConditionalOnProperty(
    prefix = "super-controller.versioning",
    name = ["enabled"],
    havingValue = "true"
)
open class UserControllerV2(
    userService: UserService,
    override val mapper: UserResponseMapperV2
) :
    SuperController<User, Long, StoreUserRequest, UpdateUserRequest>(userService),
    BulkOperationsMarker<User, Long, StoreUserRequest, UpdateUserRequest>,
    SoftDeletableMarker<User, Long> {
    
    init {
        needAuthorization = false
    }
}
