package io.github.robertomike.super_controller.utils

import io.github.robertomike.super_controller.exceptions.SuperControllerException
import io.github.robertomike.super_controller.utils.ClassFounder.loadedClasses
import org.reflections.Reflections
import org.reflections.scanners.Scanners.SubTypes
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

interface ClassUtils {
    val generics: Array<Type>
        get() = (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments

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
