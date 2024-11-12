package io.github.robertomike.super_controller.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
class SuperControllerException : BasicException {
    constructor(reason: String?) : super(reason)
    constructor(reason: String?, e: Exception?) : super(reason, e)
}
