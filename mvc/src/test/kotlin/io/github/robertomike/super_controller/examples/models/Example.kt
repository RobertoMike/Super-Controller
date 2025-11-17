package io.github.robertomike.super_controller.examples.models

import io.github.robertomike.hefesto.models.HibernateModel
import jakarta.persistence.*

@Entity
@Table(name = "users")
data class Example(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null,
    val name: String? = null,

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    val orders: List<Order>? = null
) : HibernateModel