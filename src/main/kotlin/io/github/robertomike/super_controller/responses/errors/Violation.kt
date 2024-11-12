package io.github.robertomike.super_controller.responses.errors

data class Violation(val fieldName: String, val message: String) {
    constructor() : this("", "")
}
