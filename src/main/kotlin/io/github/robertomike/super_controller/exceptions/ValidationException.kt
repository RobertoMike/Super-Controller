package io.github.robertomike.super_controller.exceptions

import jakarta.validation.ConstraintViolation
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * Exception to fire a 404 (Not Found) HTTP status
 */
@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
class ValidationException(val errors: Set<ConstraintViolation<*>>) : BasicException("Validation failed")
