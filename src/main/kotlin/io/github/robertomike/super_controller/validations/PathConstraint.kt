package io.github.robertomike.super_controller.validations

import jakarta.validation.Constraint
import jakarta.validation.Payload
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import kotlin.reflect.KClass

@MustBeDocumented
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@NotNull
@Constraint(validatedBy = [])
@NotBlank
@Pattern(regexp = "^([a-zA-Z0-9-_]+\\.)*[a-zA-Z0-9-_]+$", message = "Path is invalid")
annotation class PathConstraint(
    val message: String = "Path is invalid",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)
