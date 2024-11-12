package io.github.robertomike.super_controller.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * Exception to fire a 404 (Not Found) HTTP status
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
class NotFoundException(reason: String?) : BasicException(reason)
