package io.github.robertomike.super_controller.utils

import io.github.robertomike.super_controller.exceptions.SuperControllerException
import io.github.robertomike.super_controller.utils.ClassFounder.loadedClasses
import org.reflections.Reflections
import org.reflections.scanners.Scanners.SubTypes

/**
 * Utility class for working with classes.
 *
 * Provides methods for finding classes by name and package.
 */
interface ClassUtils {
    /**
     * Finds a class by name and package.
     *
     * @param basePackage The base package to search in.
     * @param directory The directory to search in.
     * @param fileName The name of the class to find.
     * @param clazz The class type to find.
     * @return The found class, or throws a [SuperControllerException] if not found.
     */
    @Suppress("UNCHECKED_CAST")
    fun <O> findClass(basePackage: String, directory: String, fileName: String, clazz: Class<O>): Class<O> {
        val packagePath = "${basePackage}.$directory"
        val completeName = "$packagePath.$fileName"

        if (loadedClasses.contains(completeName))
            return loadedClasses[completeName] as Class<O>

        val oClass = try {
            Class.forName(completeName) as Class<O>
        } catch (e: ClassNotFoundException) {
            val reflections = Reflections(packagePath)
            val typeList = reflections.get(SubTypes.of(clazz).asClass<O>())

            typeList.find { type -> type.simpleName == fileName } as Class<O>?
                ?: throw SuperControllerException("Class not found $fileName in directory $directory", e)
        }

        loadedClasses[completeName] = oClass

        return oClass
    }
}
