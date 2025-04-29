package io.github.robertomike.super_controller.utils

/**
 * A utility class for managing loaded classes.
 *
 * This class provides a map of loaded classes, where each class is identified by its fully qualified name.
 *
 * @author Roberto Micheletti
 */
object ClassFounder {
    /**
     * A map of loaded classes, where each class is identified by its fully qualified name.
     */
    @JvmField
    val loadedClasses = mutableMapOf<String, Class<*>>()
}