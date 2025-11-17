package io.github.robertomike.super_controller.services.interfaces

import io.github.robertomike.super_controller.requests.Request
import io.github.robertomike.super_controller.utils.GenericUtil
import org.springframework.data.domain.PageRequest

/**
 * Interface for basic CRUD operations.
 *
 * This interface provides a set of methods for performing basic CRUD (Create, Read, Update, Delete) operations.
 * It is designed to be implemented by services that manage a specific type of data.
 *
 * @param M The type of data being managed by the service.
 * @param ID The type of identifier used to uniquely identify the data.
 */
interface BasicService<M, PAGE, ID, out SR : Request, out UR : Request, DELETE> : GenericUtil {
    /**
     * Retrieves a page of data.
     *
     * This method returns a page of data based on the provided page request.
     *
     * @param page The page request containing the pagination information.
     * @return A page of data.
     */
    fun index(page: PageRequest, params: Map<String, String>): PAGE

    /**
     * Creates a new piece of data.
     *
     * This method creates a new piece of data based on the provided request.
     *
     * @param request The request containing the data to be created.
     * @return The newly created data.
     */
    fun store(request: @UnsafeVariance SR): M

    /**
     * Retrieves a piece of data by its identifier.
     *
     * This method returns a piece of data based on its identifier.
     *
     * @param model The identifier of the data to be retrieved.
     * @return The retrieved data.
     */
    fun show(model: M): M

    /**
     * Updates a piece of data.
     *
     * This method updates a piece of data based on the provided request and identifier.
     *
     * @param model The identifier of the data to be updated.
     * @param request The request containing the updated data.
     * @return The updated data.
     */
    fun update(model: M, request: @UnsafeVariance UR): M

    /**
     * Deletes a piece of data.
     *
     * This method deletes a piece of data based on its identifier.
     *
     * @param model The identifier of the data to be deleted.
     */
    fun delete(model: M): DELETE


    fun findById(id: ID): M
}
