package io.github.robertomike.super_controller.controllers.markers

import io.github.robertomike.super_controller.exceptions.SuperControllerException
import io.github.robertomike.super_controller.models.SoftDeletableEntity
import io.github.robertomike.super_controller.requests.Request
import io.github.robertomike.super_controller.services.interfaces.BasicService
import io.github.robertomike.super_controller.services.softdelete.SoftDeletableService
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional

/**
 * Controller marker interface for soft delete operations.
 *
 * When a SuperController implements this interface, it automatically gains
 * soft delete, restore, and force delete endpoints.
 *
 * **Architecture:** This controller interface delegates to the service layer's SoftDeletableService interface.
 * The service should implement `io.github.robertomike.super_controller.services.softdelete.SoftDeletableService`
 * which provides the actual business logic with default implementations.
 *
 * **Important:** Methods are NOT annotated with Spring mappings to avoid duplicate routes.
 * Routes are registered by BaseRouter which scans for methods named: softDelete, restore, forceDelete.
 *
 * Example:
 * ```kotlin
 * // Service layer
 * class UserService(private val repository: UserRepository) : 
 *     SuperService<User, Page<User>, Long, UserRequest, UserRequest>(),
 *     SoftDeletableService<User, Page<User>, Long, UserRequest, UserRequest, Unit> {
 *     
 *     override fun getRepository() = repository
 * }
 *
 * // Controller layer
 * @RestController
 * class UserController(service: UserService) : 
 *     SuperController<User, Long, UserRequest, UserRequest>(service), 
 *     SoftDeletable<User, Long> {
 *     
 *     // Optionally override to add controller-specific logic
 *     override fun beforeSoftDelete(id: Long) {
 *         // Controller-level validation
 *     }
 * }
 * ```
 *
 * This will add the following endpoints:
 * - POST /users/{id}/soft-delete - Mark entity as deleted
 * - POST /users/{id}/restore - Restore soft deleted entity
 * - DELETE /users/{id}/force - Permanently delete entity
 *
 * @param M The type of the model (must implement SoftDeletableEntity).
 * @param ID The type of the ID.
 */
interface SoftDeletableMarker<M, ID> where M : SoftDeletableEntity {

    var service: BasicService<M, Page<M>, ID, Request, Request, Unit>
    /**
     * Get the service instance that implements SoftDeletableService.
     * This method must be provided by the implementing controller.
     */
    fun getSoftDeleteService(): SoftDeletableService<M, *, ID, *, *> {
        if (service !is SoftDeletableService<M, *, ID, *, *>) {
            throw SuperControllerException("The service must implement SoftDeletableService")
        }
        return service as SoftDeletableService<M, *, ID, *, *>
    }
    
    /**
     * Soft deletes an entity by ID (marks as deleted without removing from database).
     *
     * Delegates to service layer's softDelete method.
     *
     * @param id The ID of the entity to soft delete.
     * @return The soft deleted entity.
     */
    @Transactional
    fun softDelete(@org.springframework.web.bind.annotation.PathVariable id: ID): ResponseEntity<M> {
        beforeSoftDelete(id)
        val entity = getSoftDeleteService().softDelete(id)
        afterSoftDelete(entity)
        return ResponseEntity.ok(entity)
    }
    
    /**
     * Restores a soft deleted entity.
     *
     * Delegates to service layer's restore method.
     *
     * @param id The ID of the entity to restore.
     * @return The restored entity.
     */
    @Transactional
    fun restore(@org.springframework.web.bind.annotation.PathVariable id: ID): ResponseEntity<M> {
        beforeRestore(id)
        val entity = getSoftDeleteService().restore(id)
        afterRestore(entity)
        return ResponseEntity.ok(entity)
    }
    
    /**
     * Force deletes an entity (permanent deletion).
     *
     * Delegates to service layer's forceDelete method.
     *
     * @param id The ID of the entity to force delete.
     */
    @Transactional
    fun forceDelete(@org.springframework.web.bind.annotation.PathVariable id: ID): ResponseEntity<Void> {
        beforeForceDelete(id)
        getSoftDeleteService().forceDelete(id)
        afterForceDelete(id)
        return ResponseEntity.noContent().build()
    }
    
    // Controller-level hook methods that can be overridden
    
    /**
     * Called before soft deleting an entity (controller level).
     * Override to add controller-specific validation or logic.
     */
    fun beforeSoftDelete(id: ID) {}

    /**
     * Called after soft deleting an entity (controller level).
     * Override to add controller-specific logic like response transformation.
     */
    fun afterSoftDelete(entity: M) {}

    /**
     * Called before restoring an entity (controller level).
     * Override to add controller-specific validation or logic.
     */
    fun beforeRestore(id: ID) {}

    /**
     * Called after restoring an entity (controller level).
     * Override to add controller-specific logic like response transformation.
     */
    fun afterRestore(entity: M) {}

    /**
     * Called before force deleting an entity (controller level).
     * Override to add controller-specific validation or logic.
     */
    fun beforeForceDelete(id: ID) {}

    /**
     * Called after force deleting an entity (controller level).
     * Override to add controller-specific logic.
     */
    fun afterForceDelete(id: ID) {}
}
