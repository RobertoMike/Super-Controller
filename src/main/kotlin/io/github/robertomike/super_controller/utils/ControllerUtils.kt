package io.github.robertomike.super_controller.utils

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.robertomike.super_controller.config.ConfigProperties
import io.github.robertomike.super_controller.enums.Methods
import io.github.robertomike.super_controller.enums.Methods.*
import io.github.robertomike.super_controller.exceptions.BasicException
import io.github.robertomike.super_controller.exceptions.ServerException
import io.github.robertomike.super_controller.exceptions.SuperControllerException
import io.github.robertomike.super_controller.exceptions.ValidationException
import io.github.robertomike.super_controller.policies.Policy
import io.github.robertomike.super_controller.requests.Request
import io.github.robertomike.super_controller.responses.Response
import io.github.robertomike.super_controller.services.BasicService
import jakarta.validation.Validator
import org.springframework.util.StringUtils

abstract class ControllerUtils : ClassUtils {
    abstract var properties: ConfigProperties
    abstract var mapper: ObjectMapper
    abstract var validator: Validator
    abstract val nameModel: String
    abstract var basePackage: String?

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

    open fun setConfig() {
    }

    open fun onlyUrls(): List<Methods> {
        return listOf(INDEX, STORE, SHOW, UPDATE, DESTROY)
    }

    open fun exceptUrls(): List<Methods> {
        return listOf()
    }

    open fun updateRequest(): Class<out Request>? {
        return null
    }

    open fun storeRequest(): Class<out Request>? {
        return null
    }

    open fun listResponse(): Class<out Response>? {
        return null
    }

    open fun detailResponse(): Class<out Response>? {
        return null
    }

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
        } catch (e: Exception) {
            throw SuperControllerException("The json is not valid", e)
        }
    }

    fun findResponseFor(method: Methods?): Class<out Response>? {
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

    private fun validateRequest(body: Request) {
        val violations = HashSet(validator.validate(body))

        if (violations.isNotEmpty()) {
            throw ValidationException(violations)
        }
    }
}