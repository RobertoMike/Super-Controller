package io.github.robertomike.super_controller.utils

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.robertomike.super_controller.config.ConfigProperties
import io.github.robertomike.super_controller.enums.Methods
import io.github.robertomike.super_controller.enums.Methods.*
import io.github.robertomike.super_controller.exceptions.BasicException
import io.github.robertomike.super_controller.exceptions.ServerException
import io.github.robertomike.super_controller.exceptions.SuperControllerException
import io.github.robertomike.super_controller.policies.Policy
import io.github.robertomike.super_controller.requests.Request
import io.github.robertomike.super_controller.responses.Response
import io.github.robertomike.super_controller.services.BasicService
import jakarta.validation.ConstraintViolationException
import jakarta.validation.Validator
import org.springframework.util.StringUtils

/**
 * Abstract class providing utility methods for controllers.
 *
 * This class provides methods for finding classes, validating requests, and
 * other utility functions for controllers.
 *
 */
abstract class ControllerUtils : ClassUtils {
    /**
     * Sets the configuration properties for this controller.
     */
    abstract var properties: ConfigProperties
    /**
     * Sets the object mapper for JSON serialization and deserialization.
     */
    abstract var mapper: ObjectMapper
    /**
     * Sets the validator for request validation.
     */
    abstract var validator: Validator
    /**
     * Gets the name of the model associated with this controller.
     */
    abstract val nameModel: String
    /**
     * Gets or sets the base package for class lookup.
     */
    abstract var basePackage: String?

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
            Response::class.java -> properties.path.responses
            Policy::class.java -> properties.path.policies
            BasicService::class.java -> properties.path.services
            else -> throw SuperControllerException("Class not supported")
        }

        if (basePackage == null) {
            throw SuperControllerException("The base package is not defined")
        }

        return findClass(basePackage!!, packageClass, fileName, clazz)
    }

    val urls: List<Methods>
        get() {
            val urls = onlyUrls()
            if (urls.isEmpty()) {
                throw ServerException("Urls cannot be empty")
            }

            val finalUrls = urls.filter { url ->
                !exceptUrls().contains(url)
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
     * Returns a list of HTTP methods that are only allowed for this controller.
     *
     * @return A list of HTTP methods that are only allowed for this controller.
     */
    open fun onlyUrls(): List<Methods> {
        return listOf(INDEX, STORE, SHOW, UPDATE, DESTROY)
    }

    /**
     * Returns a list of HTTP methods that are excluded for this controller.
     *
     * @return A list of HTTP methods that are excluded for this controller.
     */
    open fun exceptUrls(): List<Methods> {
        return listOf()
    }

    /**
     * Returns the request class for update operations.
     *
     * @return The request class for update operations, or null if not defined.
     */
    open fun updateRequest(): Class<out Request>? {
        return null
    }

    /**
     * Returns the request class for store operations.
     *
     * @return The request class for store operations, or null if not defined.
     */
    open fun storeRequest(): Class<out Request>? {
        return null
    }

    /**
     * Returns the response class for list operations.
     *
     * @return The response class for list operations, or null if not defined.
     */
    open fun listResponse(): Class<out Response>? {
        return null
    }

    /**
     * Returns the response class for detail operations.
     *
     * @return The response class for detail operations, or null if not defined.
     */
    open fun detailResponse(): Class<out Response>? {
        return null
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
    fun findRequestFor(method: Methods, json: String): Request {
        var requestClass = when (method) {
            STORE -> storeRequest()
            UPDATE -> updateRequest()
            else -> throw SuperControllerException("Method not supported")
        }
        if (requestClass == null) {
            val methodName = StringUtils.capitalize(method.name.lowercase())

            requestClass = findClass(
                methodName + nameModel + properties.classSuffix.request,
                Request::class.java
            )
        }

        try {
            val mappedRequest = mapper.readValue(json, requestClass)

            validateRequest(mappedRequest)

            return mappedRequest
        } catch (e: BasicException) {
            throw e
        }  catch (e: ConstraintViolationException) {
            throw e
        } catch (e: Exception) {
            throw SuperControllerException("The json is not valid", e)
        }
    }

    /**
     * Finds the response class for the given method.
     *
     * This method attempts to find a response class that matches the given method. If a matching class is found, it is returned. If no matching class is found, the method attempts to find a default response class. If no default response class is found, null is returned.
     *
     * @param method The method for which to find a response class
     * @return The response class, or null if not found
     * @throws SuperControllerException If the method is not supported
     */
    fun findResponseFor(method: Methods): Class<out Response>? {
        val responseClass = when (method) {
            INDEX -> listResponse()
            SHOW, UPDATE, STORE -> detailResponse()
            else -> throw SuperControllerException("Method not supported")
        }

        return try {
            responseClass ?: findClass(
                nameModel + properties.classSuffix.response,
                Response::class.java
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Validates the request for the given method and request object.
     *
     * This method checks if the request object is valid for the given method.
     * If the request is invalid, a [ConstraintViolationException] is thrown.
     *
     * @param body The request object to validate.
     */
    private fun validateRequest(body: Request) {
        val violations = validator.validate(body)

        if (violations.isNotEmpty()) {
            throw ConstraintViolationException(violations)
        }
    }
}