package io.github.robertomike.super_controller.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.robertomike.springrules.utils.getBeanByClassOrName
import io.github.robertomike.super_controller.config.ConfigProperties
import io.github.robertomike.super_controller.enums.Methods
import io.github.robertomike.super_controller.enums.Methods.DESTROY
import io.github.robertomike.super_controller.enums.Methods.INDEX
import io.github.robertomike.super_controller.enums.Methods.SHOW
import io.github.robertomike.super_controller.enums.Methods.STORE
import io.github.robertomike.super_controller.enums.Methods.UPDATE
import io.github.robertomike.super_controller.exceptions.BasicException
import io.github.robertomike.super_controller.exceptions.ServerException
import io.github.robertomike.super_controller.exceptions.SuperControllerException
import io.github.robertomike.super_controller.exceptions.UnauthorizedException
import io.github.robertomike.super_controller.mappers.ResponseMapper
import io.github.robertomike.super_controller.policies.Policy
import io.github.robertomike.super_controller.requests.Request
import io.github.robertomike.super_controller.responses.Response
import io.github.robertomike.super_controller.services.BasicService
import io.github.robertomike.super_controller.utils.ClassUtils
import io.github.robertomike.super_controller.utils.GenericUtil
import io.github.robertomike.super_controller.utils.RouterUtil
import jakarta.validation.ConstraintViolationException
import jakarta.validation.ValidationException
import jakarta.validation.Validator
import org.atteo.evo.inflector.English
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.http.HttpStatus
import org.springframework.util.StringUtils
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import java.util.*

/**
 * Abstract class providing utility methods for controllers.
 *
 * This class provides methods for finding classes, validating requests, and
 * other utility functions for controllers.
 *
 */
abstract class BaseController<M, ID, R, PAGE> : ClassUtils, GenericUtil {
    /**
     * Sets the configuration properties for this controller.
     */
    @Autowired
    lateinit var properties: ConfigProperties

    /**
     * Sets the object mapper for JSON serialization and deserialization.
     */
    @Autowired
    lateinit var objectMapper: ObjectMapper

    /**
     * Sets the validator for request validation.
     */
    @Autowired
    lateinit var validator: Validator

    /**
     * Utility for handling routing registrations and operations in the application.
     *
     * This variable is an instance of [RouterUtil], which facilitates the dynamic
     * registration of CRUD routes for controllers. It is used internally to manage
     * route mappings and configurations.
     *
     * The instance is injected using Spring's `@Autowired` mechanism and ensures
     * that the controller routes are properly registered and accessible.
     */
    @Autowired
    lateinit var routerUtil: RouterUtil

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
    var policy: Policy<ID, Request, Request>? = null

    /**
     * Returns the request class for update operations.
     */
    private var updateRequest: Class<out Request>? = null

    /**
     * Returns the request class for store operations.
     */
    private var storeRequest: Class<out Request>? = null

    /**
     * Returns a list of HTTP methods that are only allowed for this controller.
     */
    var onlyUrls = mutableListOf(INDEX, STORE, SHOW, UPDATE, DESTROY)

    /**
     * Returns a list of HTTP methods that are excluded for this controller.
     */
    var exceptUrls = mutableListOf<Methods>()

    /**
     * The name of the model in plurar
     */
    private val pluralNameModel: String
        get() {
            return English.plural(nameModel)
        }

    /**
     * The base URL for the controller
     */
    open val baseUrl: String
        get() {
            return properties.prefixUrl + pluralNameModel.lowercase(Locale.getDefault())
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
     * Handles the index action, returning a list of models.
     *
     * @param page The page number for pagination
     * @param size The page size for pagination
     * @return A list of models
     */
    abstract fun index(
        @RequestParam(value = "page", defaultValue = "0") page: Int,
        @RequestParam(value = "size", defaultValue = "10") size: Int
    ): PAGE

    /**
     * Handles the store action, creating a new model.
     *
     * @param json The JSON data for the new model
     */
    @ResponseStatus(HttpStatus.CREATED)
    abstract fun store(@RequestBody json: String): R

    /**
     * Handles the show action, returning a single model by ID.
     *
     * @param id The ID of the model to retrieve
     * @return The model
     */
    abstract fun show(@PathVariable id: ID): R

    /**
     * Handles the update action, updating an existing model.
     *
     * @param id The ID of the model to update
     * @param json The JSON data for the updated model
     */
    abstract fun update(@PathVariable id: ID, @RequestBody json: String): R

    /**
     * Handles the destroy action, deleting a model by ID.
     *
     * @param id The ID of the model to delete
     */
    abstract fun destroy(@PathVariable id: ID)

    /**
     * Executes the policy for the given method and ID.
     *
     * @param method The method to execute (e.g. Methods.INDEX, Methods.STORE, etc.)
     * @param id The ID of the model (optional)
     * @param request The request data (optional)
     */
    fun executePolicy(method: Methods, id: ID? = null, request: Request? = null) {
        if (!needAuthorization) {
            return
        }

        val policy = policy ?: throw SuperControllerException("Policy not found")

        if (method in listOf(SHOW, UPDATE, DESTROY) && id == null) {
            throw SuperControllerException("Id cannot be null")
        }
        if (method in listOf(STORE, UPDATE) && request == null) {
            throw SuperControllerException("Request cannot be null")
        }

        val authorized: Boolean = when (method) {
            INDEX -> policy.viewAll()
            STORE -> policy.store(request!!)
            SHOW -> policy.view(id!!)
            UPDATE -> policy.update(id!!, request!!)
            DESTROY -> policy.destroy(id!!)
        }

        if (!authorized) {
            throw UnauthorizedException("Unauthorized access to " + method.name + " for " + nameModel)
        }
    }

    /**
     * Validates the request for the given method and request object.
     *
     * This method checks if the request object is valid for the given method.
     * If the request is invalid, a [ConstraintViolationException] is thrown.
     *
     * @param body The request objects to validate.
     */
    private fun validateRequest(body: Request) {
        val violations = validator.validate(body)

        if (violations.isNotEmpty()) {
            throw ConstraintViolationException(violations)
        }
    }

    /**
     * Finds a request class for the given method and JSON string.
     *
     * This method attempts to find a request class that matches the given method and
     * JSON string. If a matching class is found, it is used to deserialize the JSON
     * string into a request object. If no matching class is found, a
     * [SuperControllerException] is thrown.
     *
     * @param method The HTTP method for which to find a request class.
     * @param json The JSON string to deserialize into a request object.
     * @return The deserialized request object, or throws a [SuperControllerException] if no matching class is found.
     * @throws SuperControllerException If no matching request class is found, or if the JSON string is invalid.
     */
    fun findRequestForAndMap(method: Methods, json: String): Request {
        var requestClass = when (method) {
            STORE -> storeRequest
            UPDATE -> updateRequest
            else -> throw SuperControllerException("Method not supported")
        }

        if (requestClass == null) {
            val methodName = StringUtils.capitalize(method.name.lowercase())

            requestClass = findRequestClass(methodName)

            saveRequestClass(method, requestClass)
        }

        try {
            val mappedRequest = objectMapper.readValue(json, requestClass)

            validateRequest(mappedRequest)

            return mappedRequest
        } catch (e: BasicException) {
            throw e
        } catch (e: ValidationException) {
            throw e
        } catch (e: ConstraintViolationException) {
            throw e
        } catch (e: Exception) {
            throw SuperControllerException("The json is not valid", e)
        }
    }

    /**
     * Finds and returns the request class corresponding to the given method name.
     *
     * This method attempts to locate the request class using a specific naming convention
     * based on the method name, combined with the controller's model name and a class suffix.
     * If the primary naming convention fails, it tries an alternative approach by uncapping
     * the model name and appending it to the method name with the request class suffix.
     *
     * @param methodName The name of the method for which the request class needs to be found.
     * @return The `Class` object representing the found request class.
     */
    private fun findRequestClass(methodName: String): Class<Request> {
        return try {
            findClass(
                methodName + nameModel + properties.classSuffix.request,
                Request::class.java
            )
        } catch (e: Exception) {
            findClass(
                StringUtils.uncapitalize(nameModel) + ".$methodName${properties.classSuffix.request}",
                Request::class.java
            )
        }
    }

    /**
     * Saves the request class corresponding to the specified method.
     *
     * @param method The HTTP method (e.g., STORE, UPDATE) associated with the request class.
     * @param requestClass The class of the request to be stored for the given method.
     * @throws SuperControllerException If the method is not supported.
     */
    private fun saveRequestClass(method: Methods, requestClass: Class<Request>) {
        when (method) {
            STORE -> storeRequest = requestClass
            UPDATE -> updateRequest = requestClass
            else -> throw SuperControllerException("Method not supported")
        }
    }

    /**
     * Finds a class by file name and class type.
     *
     * @param fileName Name of the file to find.
     * @param clazz Class type to find.
     * @return The found class, or throws a [SuperControllerException] if not found.
     */
    fun <O> findClass(fileName: String, clazz: Class<O>): Class<O> {
        val packageClass = when (clazz) {
            Request::class.java -> properties.path.requests
            Policy::class.java -> properties.path.policies
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