package io.github.robertomike.super_controller.services.softdelete

import io.github.robertomike.super_controller.models.SoftDeletableEntity
import io.github.robertomike.super_controller.repositories.RepositorySupport
import io.github.robertomike.super_controller.requests.Request
import io.github.robertomike.super_controller.services.interfaces.BasicService
import org.slf4j.LoggerFactory
import org.springframework.data.repository.Repository

/**
 * Interface for services that support soft delete operations.
 *
 * Services implementing this interface can soft delete entities (marking them as deleted
 * without removing them from the database), restore them, and force delete them permanently.
 *
 * The interface extends BasicService and provides default implementations that use a
 * repository for direct entity persistence.
 *
 * Example:
 * ```kotlin
 * class UserService(
 *     private val repository: UserRepository
 * ) : SuperService<User, Page<User>, Long, StoreUserRequest, UpdateUserRequest>(),
 *     SoftDeletableService<User, Page<User>, Long, StoreUserRequest, UpdateUserRequest> {
 *     
 *     override fun getRepository(): JpaRepository<User, Long> = repository
 *     
 *     // Default implementations are provided, but you can override for custom logic
 *     override fun softDelete(id: Long): User {
 *         // Custom validation or logic
 *         return super.softDelete(id)
 *     }
 * }
 * ```
 *
 * @param M The type of the model (must implement SoftDeletableEntity).
 * @param PAGE The type of the page result (usually Page<M>).
 * @param ID The type of the ID.
 * @param SR The type of the store request.
 * @param UR The type of the update request.
 */
interface SoftDeletableService<M, PAGE, ID, SR : Request, UR : Request> :
    BasicService<M, PAGE, ID, SR, UR, Unit> where M : SoftDeletableEntity {

    val repository: Repository<M, ID>
    var repositorySupport: RepositorySupport
    
    /**
     * Soft deletes an entity by marking it as deleted without removing from database.
     *
     * @param id The ID of the entity to soft delete.
     * @return The soft deleted entity.
     * @throws IllegalStateException if entity is already soft deleted.
     */
    fun softDelete(id: ID): M {
        val logger = LoggerFactory.getLogger(this::class.java)
        logger.debug("Service soft delete for ID: $id")
        
        val entity = findById(id)
        
        if (entity.isDeleted()) {
            throw IllegalStateException("Entity is already soft deleted")
        }
        
        beforeSoftDelete(entity)
        entity.markAsDeleted()
        repositorySupport.persist(entity, repository)
        afterSoftDelete(entity)
        
        logger.info("Entity soft deleted successfully: $id")
        return entity
    }
    
    /**
     * Restores a soft deleted entity.
     *
     * @param id The ID of the entity to restore.
     * @return The restored entity.
     * @throws IllegalStateException if entity is not soft deleted.
     */
    fun restore(id: ID): M {
        val logger = LoggerFactory.getLogger(this::class.java)
        logger.debug("Service restore for ID: $id")
        
        val entity = findById(id)
        
        if (!entity.isDeleted()) {
            throw IllegalStateException("Entity is not soft deleted")
        }
        
        beforeRestore(entity)
        entity.restore()
        repositorySupport.update(entity, repository)
        afterRestore(entity)

        logger.info("Entity restored successfully: $id")
        return entity
    }
    
    /**
     * Force deletes an entity permanently (bypasses soft delete).
     *
     * @param id The ID of the entity to force delete.
     */
    fun forceDelete(id: ID): Unit {
        val logger = LoggerFactory.getLogger(this::class.java)
        logger.debug("Service force delete for ID: $id")
        
        val entity = findById(id)
        
        beforeForceDelete(entity)
        val result = delete(entity)
        afterForceDelete(id)
        
        logger.info("Entity force deleted successfully: $id")
        return result
    }
    
    /**
     * Checks if an entity is soft deleted.
     *
     * @param id The ID of the entity to check.
     * @return True if the entity is soft deleted, false otherwise.
     */
    fun isSoftDeleted(id: ID): Boolean {
        return findById(id).isDeleted()
    }
    
    // Hook methods for customization
    
    /**
     * Called before soft deleting an entity.
     * Override to add custom validation or logic.
     */
    fun beforeSoftDelete(entity: M) {}
    
    /**
     * Called after soft deleting an entity.
     * Override to add custom logic like logging or notifications.
     */
    fun afterSoftDelete(entity: M) {}
    
    /**
     * Called before restoring an entity.
     * Override to add custom validation or logic.
     */
    fun beforeRestore(entity: M) {}
    
    /**
     * Called after restoring an entity.
     * Override to add custom logic like logging or notifications.
     */
    fun afterRestore(entity: M) {}
    
    /**
     * Called before force deleting an entity.
     * Override to add custom validation or logic.
     */
    fun beforeForceDelete(entity: M) {}
    
    /**
     * Called after force deleting an entity.
     * Override to add custom logic like logging or notifications.
     */
    fun afterForceDelete(id: ID) {}
}

