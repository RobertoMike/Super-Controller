package io.github.robertomike.super_controller.controllers

import io.github.robertomike.springrules.utils.getBeanByClassOrName
import io.github.robertomike.super_controller.config.ConfigProperties
import io.github.robertomike.super_controller.enums.Methods
import io.github.robertomike.super_controller.enums.Methods.*
import io.github.robertomike.super_controller.exceptions.ServerException
import io.github.robertomike.super_controller.exceptions.SuperControllerException
import io.github.robertomike.super_controller.mappers.ResponseMapper
import io.github.robertomike.super_controller.policies.BasePolicy
import io.github.robertomike.super_controller.requests.Request
import io.github.robertomike.super_controller.responses.Response
import io.github.robertomike.super_controller.services.interfaces.BasicService
import io.github.robertomike.super_controller.utils.ClassUtils
import io.github.robertomike.super_controller.utils.GenericUtil
import io.github.robertomike.super_controller.versioning.ApiVersion
import org.atteo.evo.inflector.English
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import java.util.*

/**
 * Abstract class providing utility methods for controllers.
 *
 * This class provides methods for finding classes, validating requests, and
 * other utility functions for controllers.
 *
 */
abstract class ControllerUtil<M, PR> : ClassUtils, GenericUtil {
    /**
     * Sets the configuration properties for this controller.
     */
    @Autowired
    lateinit var properties: ConfigProperties

    /**
     * The mapper used for to map requests and responses for business logic.
     */
    open val mapper: ResponseMapper<M, out Response, out Response>? = null

    /**
     * Gets the name of the model associated with this controller.
     */
    val nameModel: String
        get() {
            return model.simpleName
        }

    /**
     * Gets or sets the base package for class lookup.
     */
    var basePackage: String? = null
        get() {
            return field ?: properties.basePackage
        }

    /**
     * Whether authorization is required for this controller.
     */
    var needAuthorization = true

    /**
     * The policy used for authorization.
     */
    var policy: BasePolicy<M, Request, Request, PR>? = null

    /**
     * Returns a list of HTTP methods that are only allowed for this controller.
     */
    var onlyUrls = mutableListOf(INDEX, STORE, SHOW, UPDATE, DESTROY)

    /**
     * Returns a list of HTTP methods that are excluded for this controller.
     */
    var exceptUrls = mutableListOf<Methods>()

    /**
     * The name of the model in plural
     */
    var path: String? = null
        get() {
            return field ?: English.plural(nameModel)
        }

    /**
     * The base URL for the controller.
     * Automatically includes version prefix if [@ApiVersion][ApiVersion] annotation is present.
     */
    open val baseUrl: String
        get() {
            val version = this::class.java.getAnnotation(ApiVersion::class.java)
            val basePath = properties.prefixUrl + path!!.lowercase(Locale.getDefault())
            
            return if (version != null) {
                // Remove leading slash from base path if present to avoid double slashes
                val cleanBasePath = basePath.trimStart('/')
                "/${version.value}/$cleanBasePath"
            } else {
                basePath
            }
        }

    /**
     * Gets the API version from the @ApiVersion annotation if present.
     *
     * @return The API version string, or null if not annotated.
     */
    fun getApiVersion(): String? {
        return this::class.java.getAnnotation(ApiVersion::class.java)?.value
    }

    /**
     * Checks if the current API version is deprecated.
     *
     * @return True if deprecated, false otherwise.
     */
    fun isDeprecated(): Boolean {
        return this::class.java.getAnnotation(ApiVersion::class.java)?.deprecated ?: false
    }

    /**
     * Gets the sunset date for this API version.
     *
     * @return The sunset date string, or null if not specified.
     */
    fun getSunsetDate(): String? {
        val version = this::class.java.getAnnotation(ApiVersion::class.java)
        return version?.sunset?.takeIf { it.isNotBlank() }
    }

    /**
     * The class of the model declared in the generics' controller.
     */
    @Suppress("UNCHECKED_CAST")
    private val model: Class<M>
        get() = generics[0] as Class<M>


    /**
     * Provides a filtered list of `Methods` according to the following logic:
     * - Retrieves the `onlyUrls` list and throws a [ServerException] if it is empty.
     * - Filters out any URLs that exist in the `exceptUrls` list.
     * - Throws a [ServerException] if the resulting filtered list is empty.
     *
     * @throws ServerException if either the initial `onlyUrls` list is empty or the filtered list results in an empty state.
     */
    val urls: List<Methods>
        get() {
            val urls = onlyUrls
            if (urls.isEmpty()) {
                throw ServerException("Urls cannot be empty")
            }

            val finalUrls = urls.filter { url ->
                !exceptUrls.contains(url)
            }.toList()

            if (finalUrls.isEmpty()) {
                throw ServerException("After filter urls with except urls result empty")
            }

            return finalUrls
        }

    /**
     * Sets the configuration for this controller.
     */
    open fun setConfig() {
    }

    /**
     * Executes the policy for the given method and ID.
     *
     * @param method The method to execute (e.g. Methods.INDEX, Methods.STORE, etc.)
     * @param model The ID of the model (optional)
     * @param request The request data (optional)
     */
    fun executePolicy(method: Methods, model: M? = null, request: Request? = null): PR {
        if (!needAuthorization) {
            return noPolicy()
        }

        val policy = policy ?: throw SuperControllerException("Policy not found")

        if (method in listOf(SHOW, UPDATE, DESTROY) && model == null) {
            throw SuperControllerException("The model cannot be null")
        }
        if (method in listOf(STORE, UPDATE) && request == null) {
            throw SuperControllerException("Request cannot be null")
        }

        return when (method) {
            INDEX -> policy.viewAll()
            STORE -> policy.store(request!!)
            SHOW -> policy.view(model!!)
            UPDATE -> policy.update(model!!, request!!)
            DESTROY -> policy.destroy(model!!)
        }
    }

    abstract fun noPolicy(): PR

    /**
     * Finds a class by file name and class type.
     *
     * @param fileName Name of the file to find.
     * @param clazz Class type to find.
     * @return The found class, or throws a [SuperControllerException] if not found.
     */
    fun <O> findClass(fileName: String, clazz: Class<O>): Class<O> {
        val packageClass = when (clazz) {
            BasePolicy::class.java -> properties.path.policies
            BasicService::class.java -> properties.path.services
            else -> throw SuperControllerException("Class not supported")
        }

        if (basePackage == null) {
            throw SuperControllerException("The base package is not defined")
        }

        return findClass(basePackage!!, packageClass, fileName, clazz)
    }

    /**
     * Resolves a bean from the application context using the specified class name.
     *
     * @param className The fully qualified name of the class to resolve.
     * @return The resolved bean of the specified type [T].
     * @throws SuperControllerException If the class cannot be found or the bean cannot be resolved.
     */
    inline fun <reified T : Any> ApplicationContext.resolveBeanFor(className: String): T {
        return this.getBeanByClassOrName(
            findClass(
                className,
                T::class.java
            )
        ) as T
    }
}