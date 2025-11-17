package io.github.robertomike.super_controller.examples.models

import com.fasterxml.jackson.annotation.JsonIgnore
import io.github.robertomike.hefesto.models.HibernateModel
import jakarta.persistence.*

@Entity
@Table(name = "orders")
data class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null,
    var name: String? = null,
    var price: Double? = null,

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User? = null
) : HibernateModel