package io.github.robertomike.super_controller.models

import java.time.LocalDateTime

/**
 * Interface for entities that support soft deletion.
 *
 * Entities implementing this interface can be soft deleted (marked as deleted without physical removal)
 * and restored later.
 *
 * Example:
 * ```kotlin
 * @Entity
 * data class User(
 *     @Id val id: Long,
 *     val name: String,
 *     
 *     @Column(nullable = true)
 *     private var deletedAt: LocalDateTime? = null
 * ) : SoftDeletableEntity {
 *     
 *     override fun getDeletedAt(): LocalDateTime? = deletedAt
 *     
 *     override fun markAsDeleted() {
 *         deletedAt = LocalDateTime.now()
 *     }
 *     
 *     override fun restore() {
 *         deletedAt = null
 *     }
 * }
 * ```
 */
interface SoftDeletableEntity {
    /**
     * Gets the deletion timestamp.
     * @return The timestamp when the entity was soft deleted, or null if not deleted.
     */
    var deletedAt: LocalDateTime?
    
    /**
     * Marks the entity as deleted by setting the deletion timestamp to now.
     */
    fun markAsDeleted() {
        deletedAt = LocalDateTime.now()
    }
    
    /**
     * Restores the entity by clearing the deletion timestamp.
     */
    fun restore() {
        deletedAt = null
    }
    
    /**
     * Checks if the entity is soft deleted.
     * @return true if the entity has a deletion timestamp, false otherwise.
     */
    fun isDeleted(): Boolean = deletedAt != null
}
