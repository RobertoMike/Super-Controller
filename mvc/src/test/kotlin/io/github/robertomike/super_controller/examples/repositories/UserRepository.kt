package io.github.robertomike.super_controller.examples.repositories

import io.github.robertomike.super_controller.examples.models.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByName(name: String): Optional<User?>
}
