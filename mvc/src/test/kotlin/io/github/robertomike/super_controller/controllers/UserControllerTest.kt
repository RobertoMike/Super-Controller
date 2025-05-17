package io.github.robertomike.super_controller.controllers

import com.fasterxml.jackson.core.type.TypeReference
import io.github.robertomike.super_controller.BasicTest
import io.github.robertomike.super_controller.examples.models.User
import io.github.robertomike.super_controller.examples.repositories.UserRepository
import io.github.robertomike.super_controller.utils.Page
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
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
            val found = userRepository.findByName("Alberto")
            assertTrue { found.isPresent }
            assertEquals(user.id, found.get().id)
        }
    }

    @Test
    @Throws(Exception::class)
    fun show() {
        makeShow(
            "$url/1",
            typeReference
        ) { user ->
            assertEquals(1L, user.id)
            assertEquals("JOHN", user.name)
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
            val found = userRepository.findById(1L)
            assertTrue(found.isPresent)
            assertEquals("Sofia", found.get().name)
            assertEquals(user.name, found.get().name)
        }
    }

    @Test
    @Throws(Exception::class)
    fun delete() {
        makeDelete("$url/1")
        val found = userRepository.findById(1L)
        assertFalse(found.isPresent)
    }
}
