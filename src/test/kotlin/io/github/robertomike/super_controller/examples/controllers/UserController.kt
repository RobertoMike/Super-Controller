package io.github.robertomike.super_controller.examples.controllers

import io.github.robertomike.super_controller.controllers.SuperController
import io.github.robertomike.super_controller.examples.models.User
import io.github.robertomike.super_controller.examples.requests.UserRequest
import io.github.robertomike.super_controller.examples.responses.UserResponse
import io.github.robertomike.super_controller.requests.Request
import io.github.robertomike.super_controller.responses.Response
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/assets")
class UserController : SuperController<User, Long>() {
    override fun setConfig() {
        needAuthorization = false
    }

    override fun listResponse(): Class<out Response> {
        return UserResponse::class.java
    }

    override fun detailResponse(): Class<out Response> {
        return UserResponse::class.java
    }

    override fun storeRequest(): Class<out Request> {
        return UserRequest::class.java
    }

    override fun updateRequest(): Class<out Request> {
        return UserRequest::class.java
    }
}
