package io.github.robertomike.super_controller.controllers

import com.fasterxml.jackson.core.type.TypeReference
import io.github.robertomike.super_controller.BasicTest
import io.github.robertomike.super_controller.examples.models.Order
import io.github.robertomike.super_controller.examples.repositories.OrderRepository
import io.github.robertomike.super_controller.responses.errors.BasicErrorResponse
import io.github.robertomike.super_controller.responses.errors.ValidationErrorResponse
import io.github.robertomike.super_controller.utils.Page
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class OrderControllerTest : BasicTest() {
    @Autowired
    lateinit var orderRepository: OrderRepository

    var typeReference: TypeReference<Order> = object : TypeReference<Order>() {
    }

    private val url = "/api/orders"

    @Test
    @Throws(Exception::class)
    fun index() {
        makeIndex(
            url,
            object : TypeReference<Page<Order>>() {
            }
        )
    }

    @Test
    @Throws(Exception::class)
    fun indexWithFilter() {
        makeIndex(
            "$url?price=100",
            null,
            { page -> assertFalse(page.content.isEmpty()) },
            object : TypeReference<Page<Order>>() {}
        )
    }

    @Test
    @Throws(Exception::class)
    fun store() {
        makeStore(
            url,
            """{
                "name": "admin",
                "userId": 4,
                "price": 100.05
            }""".trimIndent(),
            typeReference
        ) { order ->
            val found = orderRepository.findAllByUserId(4L)
            assertFalse(found.isEmpty())
            assertEquals(found[0].id, order.id)
            assertEquals(found[0].price, 100.05)
        }
    }

    @Test
    @Throws(Exception::class)
    fun storeNotValid() {
        val request: MockHttpServletRequestBuilder = MockMvcRequestBuilders.post(url)
        basicCall(
            request,
            """{
                "name": "admin",
                "userId": 4,
                "price": null
            }""".trimIndent(),
            object : TypeReference<ValidationErrorResponse>() {},
            { validation ->
                assertFalse(validation.violations.isEmpty())
                assertEquals("price", validation.violations[0].fieldName)
            },
            MockMvcResultMatchers.status().isUnprocessableEntity()
        )
    }

    @Test
    @Throws(Exception::class)
    fun show() {
        makeShow(
            "$url/2",
            typeReference
        ) { order ->
            assertEquals(2L, order.id)
            assertEquals(200.0, order.price)
        }
    }

    @Test
    @Throws(Exception::class)
    fun showUnauthorized() {
        val request: MockHttpServletRequestBuilder = MockMvcRequestBuilders.get("$url/1")
        basicCall(
            request,
            """{
                "name": "admin",
                "userId": 4,
                "price": null
            }""".trimIndent(),
            object : TypeReference<BasicErrorResponse>() {},
            { error -> assertFalse(error.message.isEmpty()) },
            MockMvcResultMatchers.status().isForbidden()
        )
    }

    @Test
    @Throws(Exception::class)
    fun update() {
        makePut(
            "$url/2",
            """{
                "name": "admin",
                "price": 100.05
            }""".trimIndent(),
            typeReference
        ) { order ->
            val found = orderRepository.findById(2L)
            assertFalse(found.isEmpty)
            assertEquals(found.get().id, order.id)
            assertEquals(found.get().price, 100.05)
        }
    }

    @Test
    @Throws(Exception::class)
    fun delete() {
        makeDelete("$url/2")
        val found = orderRepository.findById(2L)
        assertFalse(found.isPresent)
    }
}
