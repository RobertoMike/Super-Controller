package io.github.robertomike.super_controller.controllers.advice

import io.github.robertomike.super_controller.exceptions.BasicException
import io.github.robertomike.super_controller.exceptions.ValidationException
import io.github.robertomike.super_controller.responses.errors.BasicErrorResponse
import io.github.robertomike.super_controller.responses.errors.ValidationErrorResponse
import io.github.robertomike.super_controller.responses.errors.Violation
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.http.ResponseEntity
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus

@Configuration
@ControllerAdvice
@ConditionalOnProperty("super-controller.controller-advice.enable")
open class ErrorHandlingControllerAdvice {
    /**
     * Handles [ValidationException] by converting it into a [ValidationErrorResponse] and returning it as a [ResponseEntity].
     *
     * @param e the [ValidationException] to handle
     * @return a [ResponseEntity] containing a [ValidationErrorResponse] with the violations of the [ValidationException] and the appropriate HTTP status code.
     */
    @ExceptionHandler(ValidationException::class)
    @ResponseStatus
    @ResponseBody
    fun validationException(e: ValidationException): ResponseEntity<ValidationErrorResponse> {
        val error = ValidationErrorResponse()

        for (violation in e.errors) {
            error.violations.add(
                Violation(violation.propertyPath.toString(), violation.message)
            )
        }

        return ResponseEntity(error, e.status)
    }

    /**
     * Handles [BasicException] by converting it into a [ErrorResponse] and returning it as a [ResponseEntity].
     *
     * @param e the [BasicException] to handle
     * @return a [ResponseEntity] containing an [ErrorResponse] with the message of the [BasicException] and the appropriate HTTP status code.
     */
    @ExceptionHandler(BasicException::class)
    @ResponseStatus
    @ResponseBody
    fun basicException(e: BasicException): ResponseEntity<BasicErrorResponse> {
        val error = BasicErrorResponse(e.message ?: "")

        return ResponseEntity(error, e.status)
    }
}