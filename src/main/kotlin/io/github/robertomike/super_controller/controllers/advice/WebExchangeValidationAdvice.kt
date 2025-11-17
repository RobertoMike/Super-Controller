package io.github.robertomike.super_controller.controllers.advice

import io.github.robertomike.springrules.advice.ConstraintViolationAdvice
import io.github.robertomike.springrules.configs.SpringRulesConfig
import io.github.robertomike.springrules.responses.Violations
import jakarta.validation.ConstraintViolation
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.support.WebExchangeBindException

/**
 * Controller advice for handling exceptions and providing a standardized error response.
 *
 * This class provides a centralized way to handle exceptions thrown by controllers and return a standardized error response.
 */
@ControllerAdvice
@ConditionalOnProperty("super-controller.controller-advice.enable", matchIfMissing = true)
open class WebExchangeValidationAdvice(val config: SpringRulesConfig) {
    val violationAdvice = ConstraintViolationAdvice(config)

    @ExceptionHandler(WebExchangeBindException::class)
    @ResponseStatus
    @ResponseBody
    fun webExchangeBindException(e: WebExchangeBindException): ResponseEntity<Violations> {
        val constraints = e.bindingResult.allErrors.map { it.unwrap(ConstraintViolation::class.java) }
        val errors = Violations().apply {
            constraints.forEach {
                addError(violationAdvice.getPropertyPath(it.propertyPath), it.message, config.violationBody)
            }
        }

        return ResponseEntity(errors, HttpStatus.BAD_REQUEST)
    }
}