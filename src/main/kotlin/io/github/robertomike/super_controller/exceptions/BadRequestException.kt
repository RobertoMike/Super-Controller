package io.github.robertomike.super_controller.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * Exception to fire a 400 (Bad Request) HTTP status
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
class BadRequestException(reason: String?) : BasicException(reason)
