package io.github.robertomike.super_controller.policies

import io.github.robertomike.super_controller.requests.Request

/**
 * Abstract policy class that defines the basic CRUD operations.
 *
 * This class provides a basic structure for implementing policies that control access to resources.
 * It defines five abstract functions that must be implemented by concrete policy classes:
 * - [viewAll]: determines whether the current user can view all resources.
 * - [store]: determines whether the current user can store a new resource.
 * - [view]: determines whether the current user can view a specific resource.
 * - [update]: determines whether the current user can update a specific resource.
 * - [destroy]: determines whether the current user can destroy a specific resource.
 *
 * @param <ID> the type of the resource identifier.
 */
abstract class Policy<ID, out SR: Request, out UR: Request> {
    /**
     * Determines whether the current user can view all resources.
     *
     * @return true if the user can view all resources, false otherwise.
     */
    abstract fun viewAll(): Boolean

    /**
     * Determines whether the current user can store a new resource.
     *
     * @param request the request containing the new resource data.
     * @return true if the user can store the resource, false otherwise.
     */
    abstract fun store(request: @UnsafeVariance SR): Boolean

    /**
     * Determines whether the current user can view a specific resource.
     *
     * @param id the identifier of the resource to view.
     * @return true if the user can view the resource, false otherwise.
     */
    abstract fun view(id: ID): Boolean

    /**
     * Determines whether the current user can update a specific resource.
     *
     * @param id the identifier of the resource to update.
     * @param request the request containing the updated resource data.
     * @return true if the user can update the resource, false otherwise.
     */
    abstract fun update(id: ID, request: @UnsafeVariance UR): Boolean

    /**
     * Determines whether the current user can destroy a specific resource.
     *
     * @param id the identifier of the resource to destroy.
     * @return true if the user can destroy the resource, false otherwise.
     */
    abstract fun destroy(id: ID): Boolean
}
