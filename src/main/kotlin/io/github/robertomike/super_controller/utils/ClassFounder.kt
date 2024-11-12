package io.github.robertomike.super_controller.utils

object ClassFounder {
    @JvmField
    val loadedClasses = mutableMapOf<String, Class<*>>()
}