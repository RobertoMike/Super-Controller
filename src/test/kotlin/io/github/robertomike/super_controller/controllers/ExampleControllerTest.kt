package io.github.robertomike.super_controller.controllers

import com.fasterxml.jackson.core.type.TypeReference
import io.github.robertomike.super_controller.responses.errors.BasicErrorResponse
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import io.github.robertomike.super_controller.BasicTest
import kotlin.test.assertFalse

class ExampleControllerTest : BasicTest() {
    private val url = "/api/examples"

    @Test
    @Throws(Exception::class)
    fun index() {
        val request: MockHttpServletRequestBuilder = MockMvcRequestBuilders.get(url)
        basicCall(
            request,
            null,
            object : TypeReference<BasicErrorResponse>() {
            },
            { error -> assertFalse { error.message.isEmpty() } },
            MockMvcResultMatchers.status().isInternalServerError()
        )
    }

    @Test
    @Throws(Exception::class)
    fun store() {
        val request: MockHttpServletRequestBuilder = MockMvcRequestBuilders.post(url)
        basicCall(
            request,
            "{}",
            object : TypeReference<BasicErrorResponse>() {
            },
            { error -> assertFalse { error.message.isEmpty() } },
            MockMvcResultMatchers.status().isInternalServerError()
        )
    }

    @Test
    @Throws(Exception::class)
    fun show() {
        val request: MockHttpServletRequestBuilder = MockMvcRequestBuilders.get("$url/1")
        basicCall(
            request,
            null,
            object : TypeReference<BasicErrorResponse>() {
            },
            { error -> assertFalse { error.message.isEmpty() } },
            MockMvcResultMatchers.status().isInternalServerError()
        )
    }

    @Test
    @Throws(Exception::class)
    fun update() {
        val request: MockHttpServletRequestBuilder = MockMvcRequestBuilders.put("$url/1")
        basicCall(
            request,
            "{}",
            object : TypeReference<BasicErrorResponse>() {
            },
            { error -> assertFalse { error.message.isEmpty() } },
            MockMvcResultMatchers.status().isInternalServerError()
        )
    }

    @Test
    @Throws(Exception::class)
    fun delete() {
        val request: MockHttpServletRequestBuilder = MockMvcRequestBuilders.delete("$url/1")
        basicCall(
            request,
            null,
            object : TypeReference<BasicErrorResponse>() {
            },
            { error -> assertFalse { error.message.isEmpty() } },
            MockMvcResultMatchers.status().isInternalServerError()
        )
    }
}
