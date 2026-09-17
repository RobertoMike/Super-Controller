package io.github.robertomike.super_controller.controllers.markers

import io.github.robertomike.super_controller.exceptions.SuperControllerException
import io.github.robertomike.super_controller.requests.Request
import io.github.robertomike.super_controller.services.bulk.BulkResult
import io.github.robertomike.super_controller.services.bulk.BulkDeleteResult
import io.github.robertomike.super_controller.services.interfaces.BasicService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import io.github.robertomike.super_controller.services.bulk.BulkOperations as BulkOperationsService
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RequestBody

/**
 * Controller marker interface for bulk operations.
 *
 * When a SuperController implements this interface, it automatically gains
 * bulk create, update, and delete endpoints with proper generic types.
 *
 * **Architecture:** This controller interface delegates to the service layer's BulkOperations interface.
 * The service should implement `io.github.robertomike.super_controller.services.bulk.BulkOperations`
 * which provides the actual business logic with default implementations.
 *
 * **Important:** Methods are NOT annotated with Spring mappings to avoid duplicate routes.
 * Routes are registered by BaseRouter which scans for methods named: bulkStore, bulkUpdate, bulkDelete.
 *
 * Example:
 * ```kotlin
 * // Service layer
 * class UserService(private val repository: UserRepository) : 
 *     SuperService<User, Page<User>, Long, StoreUserRequest, UpdateUserRequest>(),
 *     BulkOperations<User, Page<User>, Long, StoreUserRequest, UpdateUserRequest, Unit>
 *
 * // Controller layer
 * @RestController
 * class UserController(service: UserService) : 
 *     SuperController<User, Long, StoreUserRequest, UpdateUserRequest>(service), 
 *     BulkOperations<User, Long, StoreUserRequest, UpdateUserRequest> {
 *     
 *     // Optionally override to add controller-specific logic
 *     override fun beforeBulkStore(request: StoreUserRequest) {
 *         // Controller-level validation
 *     }
 * }
 * ```
 *
 * This will add the following endpoints:
 * - POST /users/bulk - Bulk create
 * - PUT /users/bulk - Bulk update
 * - DELETE /users/bulk - Bulk delete
 *
 * @param M The type of the model.
 * @param ID The type of the ID.
 * @param SR The store request type (for creating entities).
 * @param UR The update request type (for updating entities).
 */
interface BulkOperationsMarker<M : Any, ID : Any, SR : Request, UR : Request> {

    var service: BasicService<M, Page<M>, ID, Request, Request, Unit>
    /**
     * Get the service instance that implements BulkOperations.
     * This method must be provided by the implementing controller.
     */
    fun getBulkService(): BulkOperationsService<M, *, ID, SR, UR> {
        if (service !is BulkOperationsService<M, *, ID, *, *>) {
            throw SuperControllerException("The service must implement BulkOperationsService")
        }
        return service as BulkOperationsService<M, *, ID, SR, UR>
    }

    /**
     * Runs a per-item controller-level hook (one of the beforeBulk/afterBulk methods
     * below), logging and continuing on failure instead of propagating it. These loops
     * run outside the service layer's own per-item try/catch, so an exception here
     * (an invalid item, or a consumer's own hook override throwing) would otherwise
     * abort the whole batch response even though the actual store/update/delete work
     * is unaffected.
     */
    private fun <T> runHookForEach(items: Iterable<T>, hookName: String, hook: (T) -> Unit) {
        val logger = LoggerFactory.getLogger(this::class.java)
        items.forEach {
            try {
                hook(it)
            } catch (e: Exception) {
                logger.warn("$hookName failed for an item: ${e.message}")
            }
        }
    }


    /**
     * Creates multiple entities in bulk.
     *
     * Delegates to service layer's bulkStore method.
     *
     * @param requests List of store requests.
     * @return Bulk operation results.
     */
    @Transactional
    fun bulkStore(@RequestBody requests: @JvmSuppressWildcards List<SR>): ResponseEntity<BulkResult<M>> {
        runHookForEach(requests, "beforeBulkStore", ::beforeBulkStore)
        val result = getBulkService().bulkStore(requests)
        runHookForEach(result.successful, "afterBulkStore", ::afterBulkStore)
        return ResponseEntity.ok(result)
    }
    
    /**
     * Updates multiple entities in bulk.
     *
     * Delegates to service layer's bulkUpdate method after converting list to map.
     *
     * @param updates List of update operations (id and request pairs).
     * @return Bulk operation results.
     */
    @Transactional
    fun bulkUpdate(@RequestBody updates: @JvmSuppressWildcards List<BulkUpdateItem<ID, UR>>): ResponseEntity<BulkResult<M>> {
        runHookForEach(updates, "beforeBulkUpdate") { beforeBulkUpdate(it.id, it.request) }

        // Convert list to map for service layer. A raw JSON null list element (an
        // invalid BulkUpdateItem) is dropped here rather than crashing the whole
        // request - the map has no slot to carry a "failed at conversion" entry for
        // it, unlike a per-item exception the service layer's own loop can catch.
        val updateMap = updates.mapNotNull { it?.let { item -> item.id to item.request } }.toMap()
        val result = getBulkService().bulkUpdate(updateMap)

        runHookForEach(result.successful, "afterBulkUpdate", ::afterBulkUpdate)
        return ResponseEntity.ok(result)
    }
    
    /**
     * Deletes multiple entities in bulk.
     *
     * Delegates to service layer's bulkDelete method.
     *
     * @param ids List of IDs to delete.
     * @return Bulk operation results with deletion statistics.
     */
    @Transactional
    fun bulkDelete(@RequestBody ids: List<ID>): ResponseEntity<BulkDeleteResult> {
        runHookForEach(ids, "beforeBulkDelete", ::beforeBulkDelete)

        val deleteResult = getBulkService().bulkDelete(ids)

        runHookForEach(ids, "afterBulkDelete", ::afterBulkDelete)
        return ResponseEntity.ok(deleteResult)
    }
    
    // Controller-level hook methods that can be overridden
    
    /**
     * Called before storing each item (controller level).
     * Override to add controller-specific validation or logic.
     */
    fun beforeBulkStore(request: SR) {}

    /**
     * Called after storing each item (controller level).
     * Override to add controller-specific logic like response transformation.
     */
    fun afterBulkStore(entity: M) {}

    /**
     * Called before updating each item (controller level).
     * Override to add controller-specific validation or logic.
     */
    fun beforeBulkUpdate(id: ID, request: UR) {}

    /**
     * Called after updating each item (controller level).
     * Override to add controller-specific logic like response transformation.
     */
    fun afterBulkUpdate(entity: M) {}

    /**
     * Called before deleting each item (controller level).
     * Override to add controller-specific validation or logic.
     */
    fun beforeBulkDelete(id: ID) {}

    /**
     * Called after deleting each item (controller level).
     * Override to add controller-specific logic.
     */
    fun afterBulkDelete(id: ID) {}
}

/**
 * Represents an item in a bulk update operation.
 *
 * @param id The ID of the entity to update.
 * @param request The update request data.
 */
data class BulkUpdateItem<ID, UR>(
    val id: ID,
    val request: UR
)
