package io.github.robertomike.super_controller.examples.models

import com.fasterxml.jackson.annotation.JsonIgnore
import io.github.robertomike.hefesto.models.HibernateModel
import io.github.robertomike.super_controller.models.SoftDeletableEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    
    @Column(nullable = true)
    var name: String? = null,
    
    @Column(nullable = true)
    var email: String? = null,

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    var orders: List<Order>? = null
) : HibernateModel, SoftDeletableEntity {
    
    @Column(nullable = true)
    override var deletedAt: LocalDateTime? = null
}