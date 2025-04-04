package io.github.robertomike.super_controller.controllers.advice

import io.github.robertomike.super_controller.exceptions.BasicException
import io.github.robertomike.super_controller.responses.errors.BasicErrorResponse
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * Controller advice for handling exceptions and providing a standardized error response.
 *
 * This class provides a centralized way to handle exceptions thrown by controllers and return a standardized error response.
 */
@Configuration
@ControllerAdvice
@ConditionalOnProperty("super-controller.controller-advice.enable")
open class ErrorHandlingControllerAdvice {
    /**
     * Handles [BasicException] by converting it into a [BasicErrorResponse] and returning it as a [ResponseEntity].
     *
     * @param e the [BasicException] to handle
     * @return a [ResponseEntity] containing an [BasicErrorResponse] with the message of the [BasicException] and the appropriate HTTP status code.
     */
    @ExceptionHandler(BasicException::class)
    @ResponseStatus
    @ResponseBody
    fun basicException(e: BasicException): ResponseEntity<BasicErrorResponse> {
        val error = BasicErrorResponse(e.message ?: "")

        return ResponseEntity(error, e.status)
    }
}