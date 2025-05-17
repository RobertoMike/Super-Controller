package io.github.robertomike.super_controller.reactive.examples.services

import io.github.robertomike.super_controller.reactive.examples.mappers.UserRequestMapper
import io.github.robertomike.super_controller.reactive.examples.models.User
import io.github.robertomike.super_controller.reactive.examples.repositories.UserRepository
import io.github.robertomike.super_controller.reactive.examples.requests.StoreUserRequest
import io.github.robertomike.super_controller.reactive.examples.requests.UpdateUserRequest
import io.github.robertomike.super_controller.services.SuperService
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
open class UserService(override val repository: UserRepository, override val mapper: UserRequestMapper) : SuperService<User, Long, StoreUserRequest, UpdateUserRequest>() {
    override fun afterShow(model: User): Mono<Unit> {
        return Mono.fromCallable {
            model.name = model.name?.uppercase()
        }
    }
}