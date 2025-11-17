package io.github.robertomike.super_controller.traits

import java.time.LocalDateTime

/**
 * Interface for entities that support soft deletion.
 *
 * Soft deletion marks entities as deleted without physically removing them from the database,
 * allowing for data recovery and maintaining referential integrity.
 *
 * Example usage:
 * ```kotlin
 * @Entity
 * data class User(
 *     @Id
 *     @GeneratedValue(strategy = GenerationType.IDENTITY)
 *     val id: Long? = null,
 *     var name: String,
 *     override var deletedAt: LocalDateTime? = null
 * ) : SoftDeletable
 * ```
 */
interface SoftDeletable {
    /**
     * The timestamp when the entity was soft deleted.
     * Null indicates the entity is not deleted.
     */
    var deletedAt: LocalDateTime?

    /**
     * Indicates whether the entity is soft deleted.
     * Returns true if [deletedAt] is not null.
     */
    val isDeleted: Boolean
        get() = deletedAt != null

    /**
     * Indicates whether the entity is active (not deleted).
     * Returns true if [deletedAt] is null.
     */
    val isActive: Boolean
        get() = deletedAt == null
}
