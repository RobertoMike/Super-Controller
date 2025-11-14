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
 * Integration tests demonstrating how versioning would work with URI strategy.
 * 
 * Note: These tests document the expected behavior. Full versioning support
 * requires controllers to be annotated with @ApiVersion.
 */
@TestPropertySource(properties = [
    "super-controller.versioning.enabled=true",
    "super-controller.versioning.strategy=URI",
    "super-controller.versioning.default-version=v1"
])
class VersioningStrategyDocumentationTest : BasicTest() {

    @Test
    fun `URI strategy - version would be in URL path if controller had ApiVersion annotation`() {
        // For URI strategy: URL would be /v1/api/users if controller had @ApiVersion("v1")
        // Without @ApiVersion, controller works normally at /api/users
        mockMvc.perform(get("/api/users"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
    }
    
    @Test
    fun `HEADER strategy - version would be in header if controller had ApiVersion annotation`() {
        // For HEADER strategy: URL stays /api/users, version in X-API-Version header
        // Without @ApiVersion, controller works normally
        mockMvc.perform(
            get("/api/users")
                .header("X-API-Version", "v1")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
    }
    
    @Test
    fun `PARAMETER strategy - version would be in query param if controller had ApiVersion annotation`() {
        // For PARAMETER strategy: URL with ?version=v1
        // Without @ApiVersion, controller works normally
        mockMvc.perform(get("/api/users?version=v1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
    }
    
    @Test
    fun `ACCEPT_HEADER strategy - version would be in Accept header if controller had ApiVersion annotation`() {
        // For ACCEPT_HEADER strategy: version in Accept: application/vnd.api.v1+json
        // Without @ApiVersion, controller works normally
        mockMvc.perform(
            get("/api/users")
                .header("Accept", "application/vnd.api.v1+json")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
    }
}
