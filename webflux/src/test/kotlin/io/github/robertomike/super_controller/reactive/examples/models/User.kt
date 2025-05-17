package io.github.robertomike.super_controller.reactive.examples.models

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table(name = "users")
data class User(
    @Id
    var id: Long? = null,
    var name: String? = null,
)