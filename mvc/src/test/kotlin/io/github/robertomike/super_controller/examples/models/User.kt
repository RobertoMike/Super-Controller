package io.github.robertomike.super_controller.examples.models

import com.fasterxml.jackson.annotation.JsonIgnore
import io.github.robertomike.hefesto.models.HibernateModel
import jakarta.persistence.*

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    var name: String? = null,

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    var orders: List<Order>? = null
) : HibernateModel