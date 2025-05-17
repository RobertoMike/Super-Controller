package io.github.robertomike.super_controller.reactive.controllers

import com.fasterxml.jackson.core.type.TypeReference
import io.github.robertomike.super_controller.BasicTest
import io.github.robertomike.super_controller.reactive.examples.models.User
import io.github.robertomike.super_controller.reactive.examples.repositories.UserRepository
import io.github.robertomike.super_controller.reactive.utils.Page
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UserControllerTest : BasicTest() {
    @Autowired
    lateinit var userRepository: UserRepository

    private var typeReference: TypeReference<User> = object : TypeReference<User>() {
    }

    private val url = "/api/users"

    @Test
    @Throws(Exception::class)
    fun index() {
        makeIndex(
            url,
            object : TypeReference<Page<User>>() {
            }
        )
    }

    @Test
    @Throws(Exception::class)
    fun store() {
        makeStore(
            url,
            """
                        {
                            "name": "Alberto"
                        }
                        
                        """.trimIndent(),
            typeReference
        ) { user ->
            StepVerifier.create(userRepository.findByName("Alberto"))
                .assertNext {
                    assertEquals(user.id, it.id)
                }
                .expectComplete().verify()

        }
    }

    @Test
    @Throws(Exception::class)
    fun show() {
        makeShow(
            "$url/6",
            typeReference
        ) { user ->
            assertEquals(6, user.id)
            assertEquals("GIO", user.name)
        }
    }

    @Test
    @Throws(Exception::class)
    fun update() {
        makePut(
            "$url/1",
            """
                        {
                            "name": "Sofia"
                        }
                        
                        """.trimIndent(),
            typeReference
        ) { user ->

            StepVerifier.create(userRepository.findById(1L))
                .assertNext {
                    assertEquals("Sofia", it.name)
                    assertEquals(user.name, it.name)
                }.expectComplete().verify()
        }
    }

    @Test
    @Throws(Exception::class)
    fun delete() {
        makeDelete("$url/7")
        StepVerifier.create(userRepository.findById(7L))
            .expectNextCount(0)
            .expectComplete().verify()
    }
}
