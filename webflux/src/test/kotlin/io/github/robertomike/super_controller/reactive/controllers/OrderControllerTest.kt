package io.github.robertomike.super_controller.reactive.controllers

import com.fasterxml.jackson.core.type.TypeReference
import io.github.robertomike.super_controller.BasicTest
import io.github.robertomike.super_controller.reactive.examples.models.Order
import io.github.robertomike.super_controller.reactive.examples.repositories.OrderRepository
import io.github.robertomike.super_controller.reactive.utils.Page
import io.github.robertomike.super_controller.responses.errors.BasicErrorResponse
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import reactor.test.StepVerifier
import java.nio.charset.Charset
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
            StepVerifier.create(orderRepository.findAllByUserId(4L))
                .recordWith { mutableListOf() }
                .expectNextCount(1)
                .consumeRecordedWith { list ->
                    assertEquals(list.size, 1)
                    list.first().let {
                        assertEquals(it.id, order.id)
                        assertEquals(it.price, 100.05)
                    }
                }
                .expectComplete().verify()
        }
    }

    @Test
    @Throws(Exception::class)
    fun storeNotValid() {
        webTestClient.post()
            .uri(url)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                """{
                    "name": "admin",
                    "userId": 4,
                    "price": null
                }""".trimIndent()
            )
            .exchange()
            .expectStatus().is4xxClientError
            .expectBody()
            .consumeWith { result ->
                assertEquals(
                    "{\"violations\":[{\"field\":\"price\",\"messages\":[\"must not be null\"],\"subfields\":null}]}",
                    result.responseBodyContent?.decodeToString()
                )
            }
    }

    @Test
    @Throws(Exception::class)
    fun show() {
        makeShow(
            "$url/4",
            typeReference
        ) { order ->
            assertEquals(4, order.id)
            assertEquals(400.0, order.price)
        }
    }

    @Test
    @Throws(Exception::class)
    fun showUnauthorized() {
        basicCall(
            HttpMethod.GET,
            "$url/1",
            """{
                "name": "admin",
                "userId": 4,
                "price": null
            }""".trimIndent(),
            object : TypeReference<BasicErrorResponse>() {},
            { error -> assertFalse(error.message.isEmpty()) }
        ) { it.isForbidden }
    }

    @Test
    @Throws(Exception::class)
    fun update() {
        makePut(
            "$url/3",
            """{
                "name": "admin",
                "price": 100.05
            }""".trimIndent(),
            typeReference
        ) { order ->
            StepVerifier.create(orderRepository.findOrderById(3L))
                .assertNext {
                    assertEquals(it.id, order.id)
                    assertEquals(it.price, 100.05)
               }
                .expectComplete().verify()
        }
    }

    @Test
    @Throws(Exception::class)
    fun delete() {
        makeDelete("$url/2")
        StepVerifier.create(orderRepository.findOrderById(2L))
            .expectNextCount(0)
            .expectComplete().verify()
    }
}
