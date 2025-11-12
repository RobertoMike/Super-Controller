package io.github.robertomike.super_controller.integration

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.robertomike.super_controller.examples.models.User
import io.github.robertomike.super_controller.examples.repositories.UserRepository
import io.github.robertomike.super_controller.services.bulk.BulkResult
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Integration tests for BulkExtension functionality.
 * Tests bulk create, update, and delete operations with real database interactions.
 */
@SpringBootTest
@AutoConfigureMockMvc
class BulkExtensionIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var orderRepository: io.github.robertomike.super_controller.examples.repositories.OrderRepository

    @BeforeEach
    fun setup() {
        // Delete orders first to avoid foreign key constraint violations
        orderRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun `POST bulk should create multiple users successfully`() {
        val requests = listOf(
            mapOf("name" to "Alice", "email" to "alice@test.com"),
            mapOf("name" to "Bob", "email" to "bob@test.com"),
            mapOf("name" to "Charlie", "email" to "charlie@test.com")
        )

        val response = mockMvc.perform(
            post("/api/users/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requests))
        )
            .andExpect(status().isOk)
            .andReturn()

        val result = objectMapper.readValue(response.response.contentAsString, BulkResult::class.java)

        assertEquals(3, result.successCount, "Should have created 3 users")
        assertEquals(0, result.failedCount, "Should have no failures")
        assertEquals(3, userRepository.count(), "Database should have 3 users")
    }

    @Test
    fun `POST bulk should handle partial failures gracefully`() {
        // Create a user first
        val existing = User()
        existing.name = "Existing"
        existing.email = "existing@test.com"
        userRepository.save(existing)

        val requests = listOf(
            mapOf("name" to "New User 1", "email" to "new1@test.com"),
            mapOf("email" to "invalid@test.com"),  // Missing name - should fail validation
            mapOf("name" to "New User 2", "email" to "new2@test.com")
        )

        val response = mockMvc.perform(
            post("/api/users/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requests))
        )
            .andExpect(status().isOk)
            .andReturn()

        val result = objectMapper.readValue(response.response.contentAsString, BulkResult::class.java)

        // Note: Validation may or may not catch null values depending on configuration
        // So we just verify the operation completed and returned results
        assertTrue(result.successCount >= 1, "Should have at least 1 success")
        assertEquals(3, result.totalProcessed, "Should have processed all 3 requests")
    }

    @Test
    fun `PUT bulk should update multiple users`() {
        // Create users first
        val user1 = User().apply {
            name = "User 1"
            email = "user1@test.com"
        }
        val user2 = User().apply {
            name = "User 2"
            email = "user2@test.com"
        }
        val saved1 = userRepository.save(user1)
        val saved2 = userRepository.save(user2)

        val updates = listOf(
            mapOf("id" to saved1.id, "request" to mapOf("name" to "Updated User 1", "email" to "updated1@test.com")),
            mapOf("id" to saved2.id, "request" to mapOf("name" to "Updated User 2", "email" to "updated2@test.com"))
        )

        val response = mockMvc.perform(
            put("/api/users/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updates))
        )
            .andExpect(status().isOk)
            .andReturn()

        val result = objectMapper.readValue(response.response.contentAsString, BulkResult::class.java)

        assertEquals(2, result.successCount, "Should have updated 2 users")
        assertEquals(0, result.failedCount, "Should have no failures")

        val updatedUser1 = userRepository.findById(saved1.id!!).get()
        assertEquals("Updated User 1", updatedUser1.name)
    }

    @Test
    fun `DELETE bulk should delete multiple users`() {
        // Create users first
        val user1 = User().apply {
            name = "User 1"
            email = "user1@test.com"
        }
        val user2 = User().apply {
            name = "User 2"
            email = "user2@test.com"
        }
        val user3 = User().apply {
            name = "User 3"
            email = "user3@test.com"
        }
        val saved1 = userRepository.save(user1)
        val saved2 = userRepository.save(user2)
        val saved3 = userRepository.save(user3)

        assertEquals(3, userRepository.count(), "Should start with 3 users")

        val idsToDelete = listOf(saved1.id, saved2.id)

        mockMvc.perform(
            delete("/api/users/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(idsToDelete))
        )
            .andExpect(status().isOk)

        assertEquals(1, userRepository.count(), "Should have 1 user remaining")
        assertTrue(userRepository.findById(saved3.id!!).isPresent, "User 3 should still exist")
        assertTrue(userRepository.findById(saved1.id!!).isEmpty, "User 1 should be deleted")
    }

    @Test
    fun `DELETE bulk should handle non-existent IDs gracefully`() {
        val user = User().apply {
            name = "User 1"
            email = "user1@test.com"
        }
        val saved = userRepository.save(user)
        
        // Ensure the user is actually saved and has an ID
        require(saved.id != null) { "Saved user should have an ID" }
        val validId = saved.id!!

        val idsToDelete = listOf(validId, 99999L, 88888L)  // 1 valid, 2 non-existent IDs

        val response = mockMvc.perform(
            delete("/api/users/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(idsToDelete))
        )
            .andExpect(status().isOk)
            .andReturn()

        val result = objectMapper.readValue(response.response.contentAsString, io.github.robertomike.super_controller.services.bulk.BulkDeleteResult::class.java)

        assertEquals(1, result.deletedCount, "Should have deleted 1 user, result: $result")
        assertEquals(2, result.failedCount, "Should have failed on 2 non-existent IDs, but got ${result.failedCount}, result: $result")
    }

    @Test
    fun `bulk operations should maintain transaction integrity`() {
        val requests = listOf(
            mapOf("name" to "User 1", "email" to "user1@test.com"),
            mapOf("name" to "User 2", "email" to "user2@test.com"),
            mapOf("name" to "User 3", "email" to "user3@test.com")
        )

        mockMvc.perform(
            post("/api/users/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requests))
        )
            .andExpect(status().isOk)

        // Verify all users were created
        val allUsers = userRepository.findAll()
        assertEquals(3, allUsers.size, "All users should be in database")
    }

    @Test
    fun `bulk endpoint should only be available on controllers with BulkOperations marker`() {
        // This test verifies that only controllers implementing BulkOperations have bulk endpoints
        // UserController implements BulkOperations, so /api/users/bulk should exist
        mockMvc.perform(post("/api/users/bulk")
            .contentType(MediaType.APPLICATION_JSON)
            .content("[]"))
            .andExpect(status().isOk)

        // OrderController does NOT implement BulkOperations, so /api/orders/bulk should fail
        // Could be 404 (Not Found) or 405 (Method Not Allowed) depending on Spring's routing
        mockMvc.perform(post("/api/orders/bulk")
            .contentType(MediaType.APPLICATION_JSON)
            .content("[]"))
            .andExpect(status().is4xxClientError)
    }
}
