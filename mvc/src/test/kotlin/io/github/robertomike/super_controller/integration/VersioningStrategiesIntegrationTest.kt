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

    @Test
    fun `versioned controllers with uri strategy should ignore Accept header version`() {
        mockMvc.perform(
            get("/api/V2/users")
                .header("Accept", "application/vnd.api.v1+json")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
    }

    @Test
    fun `versioned controllers should respond correctly`() {
        mockMvc.perform(
            get("/api/V2/users")
                .header("Accept", "application/vnd.api.v1+json")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
    }
}

/**
 * Integration tests for HEADER versioning strategy.
 * 
 * Tests that API version can be specified via custom header.
 */
@TestPropertySource(properties = [
    "super-controller.versioning.enabled=true",
    "super-controller.versioning.strategy=HEADER",
    "super-controller.versioning.header-name=X-API-Version",
    "super-controller.versioning.default-version=v2"
])
class HeaderVersioningStrategyIntegrationTest : BasicTest() {

    @Test
    fun `should route to correct version using header`() {
        // Request with V2 header should work (UserControllerV2 has @ApiVersion("V2"))
        mockMvc.perform(
            get("/api/users")
                .header("X-API-Version", "V2")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content[0].email").exists())  // V2 includes email field
    }

    @Test
    fun `should use default version when header not provided`() {
        // No header provided, should route to UserController (no @ApiVersion, so no email field)
        mockMvc.perform(get("/api/users"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content[0].email").doesNotExist())  // V1 doesn't have email field
    }

    @Test
    fun `should handle CRUD operations with version header`() {
        // Create
        val createRequest = """
            {
                "name": "Header Test User",
                "email": "header@example.com"
            }
        """
        
        val createResult = mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/users")
                .header("X-API-Version", "V2")
                .contentType("application/json")
                .content(createRequest)
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.email").value("header@example.com"))  // V2 returns email
            .andReturn()
        
        val objectMapper = ObjectMapper()
        val response = objectMapper.readValue(createResult.response.contentAsString, Map::class.java)
        val userId = response["id"]
        
        // Show
        mockMvc.perform(
            get("/api/users/$userId")
                .header("X-API-Version", "V2")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(userId))
            .andExpect(jsonPath("$.email").value("header@example.com"))  // V2 includes email
    }
}

/**
 * Integration tests for PARAMETER versioning strategy.
 * 
 * Tests that API version can be specified via query parameter.
 */
@TestPropertySource(properties = [
    "super-controller.versioning.enabled=true",
    "super-controller.versioning.strategy=PARAMETER",
    "super-controller.versioning.param-name=api-version",
    "super-controller.versioning.default-version=v2"
])
class ParameterVersioningStrategyIntegrationTest : BasicTest() {

    @Test
    fun `should route to correct version using query parameter`() {
        // Request with V2 parameter should work
        mockMvc.perform(get("/api/users?api-version=V2"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content[0].email").exists())  // V2 includes email field
    }

    @Test
    fun `should use default version when parameter not provided`() {
        // No parameter provided, should route to UserController (no email field)
        mockMvc.perform(get("/api/users"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content[0].email").doesNotExist())  // V1 doesn't have email
    }

    @Test
    fun `should handle CRUD operations with version parameter`() {
        // Create
        val createRequest = """
            {
                "name": "Param Test User",
                "email": "param@example.com"
            }
        """
        
        val createResult = mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/users?api-version=V2")
                .contentType("application/json")
                .content(createRequest)
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.email").value("param@example.com"))  // V2 returns email
            .andReturn()
        
        val objectMapper = ObjectMapper()
        val response = objectMapper.readValue(createResult.response.contentAsString, Map::class.java)
        val userId = response["id"]
        
        // Show
        mockMvc.perform(get("/api/users/$userId?api-version=V2"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(userId))
            .andExpect(jsonPath("$.email").value("param@example.com"))  // V2 includes email
    }

    @Test
    fun `should handle pagination with version parameter`() {
        mockMvc.perform(get("/api/users?api-version=V2&page=0&size=5"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.pageable.pageSize").value(5))
    }
}

/**
 * Integration tests for ACCEPT_HEADER versioning strategy.
 * 
 * Tests that API version can be specified via Accept header using vendor media types.
 */
@TestPropertySource(properties = [
    "super-controller.versioning.enabled=true",
    "super-controller.versioning.strategy=ACCEPT_HEADER",
    "super-controller.versioning.media-type-prefix=application/vnd.api",
    "super-controller.versioning.default-version=v2"
])
class AcceptHeaderVersioningStrategyIntegrationTest : BasicTest() {

    @Test
    fun `should route to correct version using Accept header`() {
        // Request with V2 in Accept header should work
        mockMvc.perform(
            get("/api/users")
                .header("Accept", "application/vnd.api.V2+json")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content[0].email").exists())  // V2 includes email field
    }

    @Test
    fun `should use default version when Accept header not provided`() {
        // No Accept header provided, should route to UserController (no email field)
        mockMvc.perform(get("/api/users"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content[0].email").doesNotExist())  // V1 doesn't have email
    }

    @Test
    fun `should handle CRUD operations with Accept header version`() {
        // Create
        val createRequest = """
            {
                "name": "Accept Header Test User",
                "email": "accept@example.com"
            }
        """
        
        val createResult = mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/users")
                .header("Accept", "application/vnd.api.V2+json")
                .contentType("application/json")
                .content(createRequest)
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.email").value("accept@example.com"))  // V2 returns email
            .andReturn()
        
        val objectMapper = ObjectMapper()
        val response = objectMapper.readValue(createResult.response.contentAsString, Map::class.java)
        val userId = response["id"]
        
        // Show
        mockMvc.perform(
            get("/api/users/$userId")
                .header("Accept", "application/vnd.api.V2+json")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(userId))
            .andExpect(jsonPath("$.email").value("accept@example.com"))  // V2 includes email
    }

    @Test
    fun `should handle different Accept header formats`() {
        // With charset
        mockMvc.perform(
            get("/api/users")
                .header("Accept", "application/vnd.api.V2+json; charset=UTF-8")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content[0].email").exists())  // V2 includes email field
    }
}

/**
 * Integration tests for URI versioning strategy.
 * 
 * Tests that API version is specified in the URL path.
 */
@TestPropertySource(properties = [
    "super-controller.versioning.enabled=true",
    "super-controller.versioning.strategy=URI",
    "super-controller.versioning.default-version=v1"
])
class UriVersioningStrategyIntegrationTest : BasicTest() {

    @Test
    fun `should route to correct version using URI`() {
        // UserControllerV2 has @ApiVersion("V2") and maps to /V2/users
        mockMvc.perform(get("/api/V2/users"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content[0].email").exists())  // V2 includes email field
    }

    @Test
    fun `should handle CRUD operations with version in URI`() {
        // Create
        val createRequest = """
            {
                "name": "URI Test User",
                "email": "uri@example.com"
            }
        """
        
        val createResult = mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/V2/users")
                .contentType("application/json")
                .content(createRequest)
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.email").value("uri@example.com"))  // V2 returns email
            .andReturn()
        
        val objectMapper = ObjectMapper()
        val response = objectMapper.readValue(createResult.response.contentAsString, Map::class.java)
        val userId = response["id"]
        
        // Show
        mockMvc.perform(get("/api/V2/users/$userId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(userId))
            .andExpect(jsonPath("$.email").value("uri@example.com"))  // V2 includes email
    }

    @Test
    fun `should ignore version headers with URI strategy`() {
        // With URI strategy, headers are ignored
        mockMvc.perform(
            get("/api/V2/users")
                .header("X-API-Version", "v1")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content[0].email").exists())  // Still returns V2 (with email)
    }

    @Test
    fun `should ignore version parameters with URI strategy`() {
        // With URI strategy, query parameters are ignored
        mockMvc.perform(get("/api/V2/users?version=v1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content[0].email").exists())  // Still returns V2 (with email)
    }
}
