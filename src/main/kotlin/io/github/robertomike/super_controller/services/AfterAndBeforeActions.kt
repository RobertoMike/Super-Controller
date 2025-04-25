package io.github.robertomike.super_controller.services

import io.github.robertomike.super_controller.requests.Request

interface AfterAndBeforeActions<M, PAGE, ID, SR : Request, UR : Request> {
    /**
     * Called after the index operation. This method can be overridden by subclasses to perform custom logic.
     *
     * @param models The page of models.
     */
    fun afterIndex(models: PAGE?) {
    }

    /**
     * Called before the store operation. This method can be overridden by subclasses to perform custom logic.
     *
     * @param model The model being stored.
     * @param request The request.
     */
    fun beforeStore(model: M, request: SR) {
    }

    /**
     * Called after the store operation. This method can be overridden by subclasses to perform custom logic.
     *
     * @param model The stored model.
     * @param request The request.
     */
    fun afterStore(model: M, request: SR) {
    }

    /**
     * Called before the show operation. This method can be overridden by subclasses to perform custom logic.
     *
     * @param id The ID of the model being shown.
     */
    fun beforeShow(id: ID) {
    }

    /**
     * Called after the show operation. This method can be overridden by subclasses to perform custom logic.
     *
     * @param model The model being shown.
     */
    fun afterShow(model: M) {
    }

    /**
     * Called before the update operation. This method can be overridden by subclasses to perform custom logic.
     *
     * @param model The model being updated.
     * @param request The request.
     */
    fun beforeUpdate(model: M, request: UR) {
    }

    /**
     * Called after the update operation. This method can be overridden by subclasses to perform custom logic.
     *
     * @param model The updated model.
     * @param request The request.
     */
    fun afterUpdate(model: M, request: UR) {
    }

    /**
     * Called before the delete operation. This method can be overridden by subclasses to perform custom logic.
     *
     * @param id The ID of the model being deleted.
     */
    fun beforeDelete(id: ID) {
    }

    /**
     * Called after the delete operation. This method can be overridden by subclasses to perform custom logic.
     *
     * @param model The deleted model.
     */
    fun afterDelete(model: M) {
    }

}