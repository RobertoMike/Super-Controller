package io.github.robertomike.super_controller.responses.errors

data class ValidationErrorResponse(val violations: MutableList<Violation> = ArrayList())
