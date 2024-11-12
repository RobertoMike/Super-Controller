package io.github.robertomike.super_controller.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

abstract class BasicException : RuntimeException {
    constructor(reason: String?) : super(reason)

    constructor(reason: String?, e: Exception?) : super(reason, e)

    val status: HttpStatus
        get() = javaClass.getAnnotation(ResponseStatus::class.java).value
}
