package io.github.robertomike.super_controller.responses.errors

/**
 * Represents a basic error response with a message.
 *
 * This class is used to provide a standardized error response in case of exceptions or errors.
 *
 * @property message the error message
 */
data class BasicErrorResponse(val message: String = "")