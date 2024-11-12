package io.github.robertomike.super_controller.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * Exception to fire a 403 (Unauthorized) HTTP status
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
class UnauthorizedException(reason: String?) : BasicException(reason)
