package io.github.robertomike.super_controller.integration

import io.github.robertomike.super_controller.examples.models.User
import io.github.robertomike.super_controller.examples.repositories.UserRepository
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
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Integration tests for SoftDeleteExtension functionality.
 * Tests soft delete, restore, and force delete operations with real database interactions.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SoftDeleteExtensionIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

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
    fun `POST soft-delete should mark user as deleted`() {
        // Create a user
        val user = User().apply {
            name = "Test User"
            email = "test@example.com"
        }
        val saved = userRepository.save(user)
        assertNull(saved.deletedAt, "User should not be deleted initially")

        // Soft delete the user
        val response = mockMvc.perform(
            post("/api/users/${saved.id}/soft-delete")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andReturn()

        // Verify deletedAt is set
        val updated = userRepository.findById(saved.id!!).get()
        assertNotNull(updated.deletedAt, "User should have deletedAt timestamp")
        assertTrue(updated.deletedAt!!.isBefore(java.time.LocalDateTime.now().plusMinutes(1)))
    }

    @Test
    fun `POST restore should clear deletedAt timestamp`() {
        // Create a soft-deleted user
        val user = User().apply {
            name = "Deleted User"
            email = "deleted@example.com"
            markAsDeleted()
        }
        val saved = userRepository.save(user)
        assertNotNull(saved.deletedAt, "User should be soft deleted")

        // Restore the user
        mockMvc.perform(
            post("/api/users/${saved.id}/restore")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)

        // Verify deletedAt is cleared
        val restored = userRepository.findById(saved.id!!).get()
        assertNull(restored.deletedAt, "User should not have deletedAt after restore")
    }

    @Test
    fun `DELETE force should permanently delete user`() {
        // Create a user
        val user = User().apply {
            name = "User to Force Delete"
            email = "forcedelete@example.com"
        }
        val saved = userRepository.save(user)
        val userId = saved.id!!

        assertEquals(1, userRepository.count(), "Should have 1 user")

        // Force delete the user
        mockMvc.perform(
            delete("/api/users/$userId/force")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isNoContent)

        // Verify user is permanently deleted
        assertEquals(0, userRepository.count(), "User should be permanently deleted")
        assertTrue(userRepository.findById(userId).isEmpty, "User should not exist in database")
    }

    @Test
    fun `soft delete should not physically remove user from database`() {
        // Create a user
        val user = User().apply {
            name = "Test User"
            email = "test@example.com"
        }
        val saved = userRepository.save(user)

        assertEquals(1, userRepository.count(), "Should have 1 user before soft delete")

        // Soft delete
        mockMvc.perform(post("/api/users/${saved.id}/soft-delete"))
            .andExpect(status().isOk)

        // User should still exist in database
        assertEquals(1, userRepository.count(), "User should still exist in database after soft delete")

        // But should have deletedAt set
        val softDeleted = userRepository.findById(saved.id!!).get()
        assertNotNull(softDeleted.deletedAt, "User should have deletedAt timestamp")
    }

    @Test
    fun `should not be able to soft delete already deleted user`() {
        // Create a soft-deleted user
        val user = User().apply {
            name = "Already Deleted"
            email = "already@example.com"
            markAsDeleted()
        }
        val saved = userRepository.save(user)

        // Try to soft delete again
        mockMvc.perform(post("/api/users/${saved.id}/soft-delete"))
            .andExpect(status().is5xxServerError)  // Should fail
    }

    @Test
    fun `should not be able to restore non-deleted user`() {
        // Create a normal (non-deleted) user
        val user = User().apply {
            name = "Normal User"
            email = "normal@example.com"
        }
        val saved = userRepository.save(user)

        // Try to restore
        mockMvc.perform(post("/api/users/${saved.id}/restore"))
            .andExpect(status().is5xxServerError)  // Should fail
    }

    @Test
    fun `force delete should work on soft-deleted users`() {
        // Create a soft-deleted user
        val user = User().apply {
            name = "Soft Deleted User"
            email = "softdeleted@example.com"
            markAsDeleted()
        }
        val saved = userRepository.save(user)
        val userId = saved.id!!

        // Force delete
        mockMvc.perform(delete("/api/users/$userId/force"))
            .andExpect(status().isNoContent)

        // Should be completely gone
        assertTrue(userRepository.findById(userId).isEmpty, "User should be permanently deleted")
    }

    @Test
    fun `soft delete endpoints should only be available on controllers with SoftDeletable marker`() {
        // UserController implements SoftDeletable, so endpoints should exist
        val user = User().apply {
            name = "Test"
            email = "test@example.com"
        }
        val saved = userRepository.save(user)

        mockMvc.perform(post("/api/users/${saved.id}/soft-delete"))
            .andExpect(status().isOk)

        // OrderController does NOT implement SoftDeletable, so endpoints should 404
        mockMvc.perform(post("/api/orders/1/soft-delete"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `complete soft delete lifecycle`() {
        // 1. Create user
        val user = User().apply {
            name = "Lifecycle Test"
            email = "lifecycle@example.com"
        }
        val saved = userRepository.save(user)
        val userId = saved.id!!

        assertNull(saved.deletedAt, "Initial user should not be deleted")

        // 2. Soft delete
        mockMvc.perform(post("/api/users/$userId/soft-delete"))
            .andExpect(status().isOk)

        var updated = userRepository.findById(userId).get()
        assertNotNull(updated.deletedAt, "User should be soft deleted")

        // 3. Restore
        mockMvc.perform(post("/api/users/$userId/restore"))
            .andExpect(status().isOk)

        updated = userRepository.findById(userId).get()
        assertNull(updated.deletedAt, "User should be restored")

        // 4. Soft delete again
        mockMvc.perform(post("/api/users/$userId/soft-delete"))
            .andExpect(status().isOk)

        // 5. Force delete
        mockMvc.perform(delete("/api/users/$userId/force"))
            .andExpect(status().isNoContent)

        // 6. Verify completely gone
        assertTrue(userRepository.findById(userId).isEmpty, "User should be permanently deleted")
    }
}

