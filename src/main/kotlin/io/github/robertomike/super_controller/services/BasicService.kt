package io.github.robertomike.super_controller.services

import io.github.robertomike.super_controller.exceptions.SuperControllerException
import io.github.robertomike.super_controller.mappers.RequestMapper
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
interface BasicService<M, PAGE, ID, out SR : Request, out UR : Request> : GenericUtil {
    /**
     * The mapper used for to map requests and responses for business logic.
     */
    val mapper: RequestMapper<M, @UnsafeVariance SR, @UnsafeVariance UR>
        get() {
            throw SuperControllerException("Mapper not implemented")
        }

    /**
     * Retrieves a page of data.
     *
     * This method returns a page of data based on the provided page request.
     *
     * @param page The page request containing the pagination information.
     * @return A page of data.
     */
    fun index(page: PageRequest): PAGE

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
     * @param id The identifier of the data to be retrieved.
     * @return The retrieved data.
     */
    fun show(id: ID): M

    /**
     * Updates a piece of data.
     *
     * This method updates a piece of data based on the provided request and identifier.
     *
     * @param id The identifier of the data to be updated.
     * @param request The request containing the updated data.
     * @return The updated data.
     */
    fun update(id: ID, request: @UnsafeVariance UR): M

    /**
     * Deletes a piece of data.
     *
     * This method deletes a piece of data based on its identifier.
     *
     * @param id The identifier of the data to be deleted.
     */
    fun delete(id: ID)


    fun findById(id: ID): M

    /**
     * Maps the request to the model for store operation.
     *
     * @param request The request.
     */
    fun mappingStore(request: @UnsafeVariance SR): M {
        return mapper.map(request)
    }

    /**
     * Maps the request to the model for update operation.
     *
     * @param request The request.
     */
    fun mappingUpdate(request: @UnsafeVariance UR, target: M) {
        mapper.update(request, target)
    }
}
