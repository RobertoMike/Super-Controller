package io.github.robertomike.super_controller.utils

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

interface GenericUtil {
    /**
     * Returns the generics of the current class.
     *
     * @return An array of [Type] representing the generics of the current class.
     */
    val generics: Array<Type>
        get() = (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments
}