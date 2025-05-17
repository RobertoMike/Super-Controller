package io.github.robertomike.super_controller.controllers

import io.github.robertomike.super_controller.enums.Methods
import io.github.robertomike.super_controller.exceptions.ServerException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus

interface CrudController<ID, RETURN, SR, UR, PAGE, DELETE> {
    /**
     * The base URL for the controller
     */
    val baseUrl: String

    /**
     * Provides a filtered list of `Methods` according to the following logic:
     * - Retrieves the `onlyUrls` list and throws a [ServerException] if it is empty.
     * - Filters out any URLs that exist in the `exceptUrls` list.
     * - Throws a [ServerException] if the resulting filtered list is empty.
     *
     * @throws ServerException if either the initial `onlyUrls` list is empty or the filtered list results in an empty state.
     */
    val urls: List<Methods>

    /**
     * Handles the index action, returning a list of models.
     *
     * @param page The page number for pagination
     * @param size The page size for pagination
     * @return A list of models
     */
    fun index(
        @RequestParam(value = "page", defaultValue = "0") page: Int,
        @RequestParam(value = "size", defaultValue = "10") size: Int
    ): PAGE

    /**
     * Handles the store action, creating a new model.
     *
     * @param request The JSON data for the new model
     */
    @ResponseStatus(HttpStatus.CREATED)
    fun store(@RequestBody request: SR): RETURN

    /**
     * Handles the show action, returning a single model by ID.
     *
     * @param id The ID of the model to retrieve
     * @return The model
     */
    fun show(@PathVariable id: ID): RETURN

    /**
     * Handles the update action, updating an existing model.
     *
     * @param id The ID of the model to update
     * @param request The JSON data for the updated model
     */
    fun update(@PathVariable id: ID, @RequestBody request: UR): RETURN

    /**
     * Handles the destroy action, deleting a model by ID.
     *
     * @param id The ID of the model to delete
     */
    fun destroy(@PathVariable id: ID): DELETE
}