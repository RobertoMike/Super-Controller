package io.github.robertomike.super_controller.services.bulk

/**
 * Result of a bulk operation.
 *
 * @param M The type of the models.
 * @param successful List of successfully processed models.
 * @param failed List of errors that occurred during processing.
 * @param totalProcessed Total number of items processed.
 */
data class BulkResult<M>(
    val successful: List<M>,
    val failed: List<BulkError> = emptyList(),
    val totalProcessed: Int = successful.size + failed.size
) {
    /**
     * Number of successfully processed items.
     */
    val successCount: Int
        get() = successful.size

    /**
     * Number of failed items.
     */
    val failedCount: Int
        get() = failed.size

    /**
     * Success rate as a percentage.
     */
    val successRate: Double
        get() = if (totalProcessed > 0) (successCount.toDouble() / totalProcessed) * 100 else 0.0

    /**
     * Whether all operations were successful.
     */
    val allSuccessful: Boolean
        get() = failedCount == 0

    /**
     * Whether any operations failed.
     */
    val anyFailed: Boolean
        get() = failedCount > 0
}

/**
 * Represents an error that occurred during a bulk operation.
 *
 * @param index The index of the item that failed (0-based).
 * @param error The error message.
 * @param details Additional error details as key-value pairs.
 */
data class BulkError(
    val index: Int,
    val error: String,
    val details: Map<String, Any>? = null
)

/**
 * Result of a bulk delete operation.
 *
 * @param deletedCount Number of successfully deleted items.
 * @param failedIds List of IDs that failed to delete.
 * @param errors List of errors that occurred during deletion.
 */
data class BulkDeleteResult(
    val deletedCount: Int,
    val failedIds: List<Any>,
    val errors: List<BulkError>
) {
    /**
     * Number of failed deletions.
     */
    val failedCount: Int
        get() = failedIds.size

    /**
     * Whether all deletions were successful.
     */
    val allSuccessful: Boolean
        get() = failedCount == 0

    /**
     * Whether any deletions failed.
     */
    val anyFailed: Boolean
        get() = failedCount > 0
}
