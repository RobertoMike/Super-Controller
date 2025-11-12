package io.github.robertomike.super_controller.services.bulk

import io.github.robertomike.super_controller.requests.Request
import io.github.robertomike.super_controller.services.interfaces.BasicService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page

/**
 * Interface for services that support bulk operations.
 *
 * Services implementing this interface gain bulk create, update, and delete capabilities
 * with default implementations that use the BasicService methods.
 *
 * Example:
 * ```kotlin
 * class UserService(
 *     private val repository: UserRepository
 * ) : SuperService<User, Long, StoreUserRequest, UpdateUserRequest>(),
 *     BulkOperations<User, Page<User>, Long, StoreUserRequest, UpdateUserRequest> {
 *     
 *     // Default implementations are provided, but you can override for custom logic
 *     override fun bulkStore(requests: List<StoreUserRequest>): BulkResult<User> {
 *         // Custom implementation
 *         return super.bulkStore(requests)
 *     }
 * }
 * ```
 *
 * @param M The type of the model.
 * @param PAGE The type of the page result (usually Page<M>).
 * @param ID The type of the ID.
 * @param SR The type of the store request.
 * @param UR The type of the update request.
 */
interface BulkOperations<M, PAGE, ID, SR : Request, UR : Request> :
    BasicService<M, PAGE, ID, SR, UR, Unit> {
    
    /**
     * Stores multiple models in a single operation.
     *
     * Default implementation processes each request sequentially, collecting successes and failures.
     *
     * @param requests List of store requests.
     * @return Result containing successful and failed operations.
     */
    fun bulkStore(requests: List<SR>): BulkResult<M> {
        val logger = LoggerFactory.getLogger(this::class.java)
        logger.debug("Service bulk store started with ${requests.size} items")
        
        val successful = mutableListOf<M>()
        val failed = mutableListOf<BulkError>()

        requests.forEachIndexed { index, request ->
            try {
                beforeBulkStore(request)
                val entity = store(request)
                afterBulkStore(entity, request)
                successful.add(entity)
            } catch (e: Exception) {
                logger.warn("Failed to store item at index $index: ${e.message}")
                failed.add(BulkError(index, e.message ?: "Unknown error"))
            }
        }

        logger.info("Service bulk store completed: ${successful.size} succeeded, ${failed.size} failed")
        return BulkResult(
            successful = successful,
            failed = failed,
            totalProcessed = requests.size
        )
    }

    /**
     * Updates multiple models in a single operation.
     *
     * Default implementation finds each entity by ID and updates it sequentially.
     *
     * @param updates Map of ID to update request.
     * @return Result containing successful and failed operations.
     */
    fun bulkUpdate(updates: Map<ID, UR>): BulkResult<M> {
        val logger = LoggerFactory.getLogger(this::class.java)
        logger.debug("Service bulk update started with ${updates.size} items")
        
        val successful = mutableListOf<M>()
        val failed = mutableListOf<BulkError>()

        updates.entries.forEachIndexed { index, (id, request) ->
            try {
                val entity = findById(id)
                beforeBulkUpdate(entity, request)
                val updated = update(entity, request)
                afterBulkUpdate(updated, request)
                successful.add(updated)
            } catch (e: Exception) {
                logger.warn("Failed to update item at index $index (ID: $id): ${e.message}")
                failed.add(BulkError(index, e.message ?: "Unknown error"))
            }
        }

        logger.info("Service bulk update completed: ${successful.size} succeeded, ${failed.size} failed")
        return BulkResult(
            successful = successful,
            failed = failed,
            totalProcessed = updates.size
        )
    }

    /**
     * Deletes multiple models in a single operation.
     *
     * Default implementation finds each entity and deletes it sequentially.
     *
     * @param ids List of IDs to delete.
     * @return Result containing deletion statistics.
     */
    fun bulkDelete(ids: List<ID>): BulkDeleteResult {
        val logger = LoggerFactory.getLogger(this::class.java)
        logger.debug("Service bulk delete started with ${ids.size} items")
        
        var successCount = 0
        val failed = mutableListOf<BulkError>()

        ids.forEachIndexed { index, id ->
            try {
                val entity = findById(id)
                beforeBulkDelete(entity)
                delete(entity)
                afterBulkDelete(id)
                successCount++
            } catch (e: Exception) {
                logger.warn("Failed to delete item at index $index (ID: $id): ${e.message}")
                failed.add(BulkError(index, e.message ?: "Unknown error"))
            }
        }

        logger.info("Service bulk delete completed: $successCount succeeded, ${failed.size} failed")
        return BulkDeleteResult(
            deletedCount = successCount,
            failedIds = ids.filterIndexed { index, _ -> failed.any { it.index == index } } as List<Any>,
            errors = failed
        )
    }
    
    // Hook methods for customization
    
    /**
     * Called before storing each item in bulk operation.
     * Override to add custom validation or logic.
     */
    fun beforeBulkStore(request: SR) {}
    
    /**
     * Called after storing each item in bulk operation.
     * Override to add custom logic like logging or notifications.
     */
    fun afterBulkStore(entity: M, request: SR) {}
    
    /**
     * Called before updating each item in bulk operation.
     * Override to add custom validation or logic.
     */
    fun beforeBulkUpdate(entity: M, request: UR) {}
    
    /**
     * Called after updating each item in bulk operation.
     * Override to add custom logic like logging or notifications.
     */
    fun afterBulkUpdate(entity: M, request: UR) {}
    
    /**
     * Called before deleting each item in bulk operation.
     * Override to add custom validation or logic.
     */
    fun beforeBulkDelete(entity: M) {}
    
    /**
     * Called after deleting each item in bulk operation.
     * Override to add custom logic like logging or notifications.
     */
    fun afterBulkDelete(id: ID) {}
}

