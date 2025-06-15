package io.github.robertomike.super_controller.services.interfaces

import io.github.robertomike.super_controller.requests.Request

interface AfterAndBeforeActions<M, PAGE, SR : Request, UR : Request, R> {
    /**
     * Called after the index operation. This method can be overridden by subclasses to perform custom logic.
     *
     * @param models The page of models.
     */
    fun afterIndex(models: PAGE?): R {
        return defaultAction()
    }

    /**
     * Called before the store operation. This method can be overridden by subclasses to perform custom logic.
     *
     * @param model The model being stored.
     * @param request The request.
     */
    fun beforeStore(model: M, request: SR): R {
        return defaultAction()
    }

    /**
     * Called after the store operation. This method can be overridden by subclasses to perform custom logic.
     *
     * @param model The stored model.
     * @param request The request.
     */
    fun afterStore(model: M, request: SR): R {
        return defaultAction()
    }

    /**
     * Called after the show operation. This method can be overridden by subclasses to perform custom logic.
     *
     * @param model The model being shown.
     */
    fun afterShow(model: M): R {
        return defaultAction()
    }

    /**
     * Called before the update operation. This method can be overridden by subclasses to perform custom logic.
     *
     * @param model The model being updated.
     * @param request The request.
     */
    fun beforeUpdate(model: M, request: UR): R {
        return defaultAction()
    }

    /**
     * Called after the update operation. This method can be overridden by subclasses to perform custom logic.
     *
     * @param model The updated model.
     * @param request The request.
     */
    fun afterUpdate(model: M, request: UR): R {
        return defaultAction()
    }

    /**
     * Called before the delete operation. This method can be overridden by subclasses to perform custom logic.
     *
     * @param id The ID of the model being deleted.
     */
    fun beforeDelete(model: M): R {
        return defaultAction()
    }

    /**
     * Called after the delete operation. This method can be overridden by subclasses to perform custom logic.
     *
     * @param model The deleted model.
     */
    fun afterDelete(model: M): R {
        return defaultAction()
    }

    fun defaultAction(): R
}