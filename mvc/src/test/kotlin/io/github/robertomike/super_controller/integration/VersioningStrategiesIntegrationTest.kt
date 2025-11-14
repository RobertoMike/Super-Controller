package io.github.robertomike.super_controller.integration

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.robertomike.super_controller.BasicTest
import org.junit.jupiter.api.Test
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import kotlin.test.assertEquals

/**
 * Integration tests for versioning when disabled (default behavior).
 * 
 * Tests that when versioning is disabled, everything works as before.
 */
@TestPropertySource(properties = [
    "super-controller.versioning.enabled=false"
])
class VersioningDisabledIntegrationTest : BasicTest() {

    @Test
    fun `should work normally when versioning is disabled`() {
        mockMvc.perform(get("/api/users"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
    }

    @Test
    fun `should not add version headers when versioning is disabled`() {
        val result = mockMvc.perform(get("/api/users"))
            .andExpect(status().isOk)
            .andReturn()
        
        // Should not have version header
        val versionHeader = result.response.getHeader("X-API-Version")
        assertEquals(null, versionHeader)
    }
    
    @Test
    fun `should handle pagination normally when versioning is disabled`() {
        mockMvc.perform(get("/api/users?page=0&size=5"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.pageable.pageSize").value(5))
    }
}

/**
 * Integration tests for ControllerUtil baseUrl behavior with versioning.
 * 
 * Tests that baseUrl correctly includes or excludes version based on strategy.
 */
@TestPropertySource(properties = [
    "super-controller.versioning.enabled=false"
])
class ControllerUtilBaseUrlIntegrationTest : BasicTest() {

    @Test
    fun `baseUrl should not include version when versioning is disabled`() {
        // UserController has version=null or versioning disabled, so baseUrl should be /api/users
        mockMvc.perform(get("/api/users"))
            .andExpect(status().isOk)
    }
    
    @Test
    fun `all CRUD operations should work with correct baseUrl`() {
        // Create operation
        val createRequest = """
            {
                "name": "Test User",
                "email": "test@example.com"
            }
        """
        
        val createResult = mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/users")
                .contentType("application/json")
                .content(createRequest)
        )
            .andExpect(status().isCreated)
            .andReturn()
        
        val objectMapper = ObjectMapper()
        val response = objectMapper.readValue(createResult.response.contentAsString, Map::class.java)
        val userId = response["id"]
        
        // Show operation
        mockMvc.perform(get("/api/users/$userId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(userId))
        
        // Index operation
        mockMvc.perform(get("/api/users"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
    }
}

/**
 * Integration tests for backward compatibility with versioning enabled.
 * 
 * These tests verify that controllers WITHOUT @ApiVersion annotation continue
 * to work normally even when versioning is enabled. This ensures backward
 * compatibility - you can enable versioning without breaking existing controllers.
 * 
 * Note: To actually test versioning strategies, controllers must be annotated
 * with @ApiVersion. The UserController used in these tests is intentionally
 * NOT annotated to test backward compatibility.
 */
@TestPropertySource(properties = [
    "super-controller.versioning.enabled=true",
    "super-controller.versioning.strategy=URI",
    "super-controller.versioning.default-version=v1"
])
class BackwardCompatibilityWithVersioningEnabledTest : BasicTest() {

    @Test
    fun `controllers without ApiVersion annotation should work normally with versioning enabled`() {
        // UserController doesn't have @ApiVersion, so it works at /api/users
        // even when versioning is enabled
        mockMvc.perform(get("/api/users"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
    }
    
    @Test
    fun `non-versioned controllers should ignore version headers`() {
        // UserController doesn't have @ApiVersion, so version header is ignored
        mockMvc.perform(
            get("/api/users")
                .header("X-API-Version", "v1")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
    }
    
    @Test
    fun `non-versioned controllers should ignore version parameters`() {
        // UserController doesn't have @ApiVersion, so version parameter is ignored
        mockMvc.perform(get("/api/users?version=v1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
    }
    
    @Test
    fun `non-versioned controllers should ignore Accept header version`() {
        // UserController doesn't have @ApiVersion, so versioned Accept header is ignored
        mockMvc.perform(
            get("/api/users")
                .header("Accept", "application/vnd.api.v1+json")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
    }
}
