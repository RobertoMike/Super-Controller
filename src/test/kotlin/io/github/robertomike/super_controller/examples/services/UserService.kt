package io.github.robertomike.super_controller.examples.services

import io.github.robertomike.super_controller.examples.mappers.UserRequestMapper
import io.github.robertomike.super_controller.examples.models.User
import io.github.robertomike.super_controller.examples.repositories.UserRepository
import io.github.robertomike.super_controller.examples.requests.StoreUserRequest
import io.github.robertomike.super_controller.services.SuperService
import org.springframework.stereotype.Service

@Service
class UserService(override val repository: UserRepository, override val mapper: UserRequestMapper) : SuperService<User, Long, StoreUserRequest, StoreUserRequest>() {
    override fun afterShow(model: User) {
        model.name = model.name?.uppercase()
    }
}