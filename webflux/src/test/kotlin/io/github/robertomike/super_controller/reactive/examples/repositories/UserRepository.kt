package io.github.robertomike.super_controller.reactive.examples.repositories

import io.github.robertomike.super_controller.reactive.examples.models.User
import io.github.robertomike.super_controller.repository.ReactivePaginatedRepository
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.util.*

@Repository
interface UserRepository : ReactivePaginatedRepository<User, Long>, ReactiveCrudRepository<User, Long> {
    fun findByName(name: String): Mono<User>
}
