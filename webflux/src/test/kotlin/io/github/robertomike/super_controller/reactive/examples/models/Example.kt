package io.github.robertomike.super_controller.reactive.examples.models

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table(name = "users")
data class Example(
    @Id
    var id: Long? = null,
    val name: String? = null,
)