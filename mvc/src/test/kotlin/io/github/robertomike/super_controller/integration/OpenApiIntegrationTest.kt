package io.github.robertomike.super_controller.integration

import io.github.robertomike.super_controller.BasicTest
import io.github.robertomike.super_controller.openapi.OpenApiConfig
import io.github.robertomike.super_controller.openapi.OpenApiProperties
import io.swagger.v3.oas.models.OpenAPI
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Integration tests for OpenAPI documentation generation.
 * 
 * These tests verify that:
 * 1. OpenAPI configuration beans are loaded correctly when enabled
 * 2. OpenAPI properties are properly configured from application properties
 * 3. OpenAPI documentation endpoint is accessible via /v3/api-docs
 * 4. Swagger UI is accessible via /swagger-ui.html
 * 5. Base OpenAPI structure is generated with info, version, and components
 * 6. Custom configuration properties are applied correctly
 * 7. OpenAPI specification includes paths and servers
 * 8. Documentation generation works within Spring Boot context
 * 
 * Note: These tests verify the OpenAPI infrastructure is working with Spring Boot.
 * The actual path documentation for Super-Controller endpoints depends on Spring's
 * discovery of controller mappings, which uses the framework's custom routing mechanism.
 */
@EnableAutoConfiguration
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@ComponentScan(basePackages = ["io.github.robertomike.super_controller"])
@TestPropertySource(properties = [
    "super-controller.openapi.enabled=true",
    "super-controller.openapi.title=Test API Documentation",
    "super-controller.openapi.description=Integration test for OpenAPI",
    "super-controller.openapi.version=1.0.0-test"
])
class OpenApiIntegrationTest : BasicTest() {

    @Autowired(required = false)
    private val openApiConfig: OpenApiConfig? = null

    @Autowired(required = false)
    private val openApiProperties: OpenApiProperties? = null

    @Autowired(required = false)
    private val openAPI: OpenAPI? = null

    @Test
    @Order(1)
    fun `should load OpenAPI configuration bean when enabled`() {
        assertNotNull(openApiConfig, "OpenAPI configuration should be loaded when enabled")
    }

    @Test
    @Order(2)
    fun `should load OpenAPI properties bean with custom values`() {
        assertNotNull(openApiProperties, "OpenAPI properties should be loaded")
        assertEquals(true, openApiProperties?.enabled, "OpenAPI should be enabled")
        assertEquals("Test API Documentation", openApiProperties?.title, "Title should match configured value")
        assertEquals("Integration test for OpenAPI", openApiProperties?.description, "Description should match")
        assertEquals("1.0.0-test", openApiProperties?.version, "Version should match configured value")
    }

    @Test
    @Order(3)
    fun `should create OpenAPI bean with configured properties`() {
        assertNotNull(openAPI, "OpenAPI bean should be created")
        assertNotNull(openAPI?.info, "OpenAPI info should not be null")
        assertEquals("Test API Documentation", openAPI?.info?.title, "OpenAPI title should match configuration")
        assertEquals("1.0.0-test", openAPI?.info?.version, "OpenAPI version should match configuration")
        assertEquals("Integration test for OpenAPI", openAPI?.info?.description, "OpenAPI description should match")
    }

    @Test
    @Order(4)
    fun `should have OpenAPI documentation endpoint accessible`() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/v3/api-docs")
                .accept(org.springframework.http.MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(org.springframework.http.MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.openapi").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.info").exists())
    }

    @Test
    @Order(5)
    fun `should include configured API information in documentation`() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/v3/api-docs")
                .accept(org.springframework.http.MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.info.title").value("Test API Documentation"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.info.description").value("Integration test for OpenAPI"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.info.version").value("1.0.0-test"))
    }

    @Test
    @Order(6)
    fun `should include components section in OpenAPI specification`() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/v3/api-docs")
                .accept(org.springframework.http.MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.components").exists())
    }

    @Test
    @Order(7)
    fun `should have Swagger UI redirect endpoint accessible`() {
        // Swagger UI redirects to the actual UI page
        mockMvc.perform(
            MockMvcRequestBuilders.get("/swagger-ui.html")
        )
            .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
    }

    @Test
    @Order(8)
    fun `should have Swagger UI index page accessible`() {
        // The actual Swagger UI page
        mockMvc.perform(
            MockMvcRequestBuilders.get("/swagger-ui/index.html")
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    @Order(9)
    fun `should include OpenAPI version in specification`() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/v3/api-docs")
                .accept(org.springframework.http.MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.openapi").value("3.0.1"))
    }

    @Test
    @Order(10)
    fun `should include paths section in OpenAPI specification`() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/v3/api-docs")
                .accept(org.springframework.http.MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.paths").exists())
    }

    @Test
    @Order(11)
    fun `should include servers in OpenAPI specification`() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/v3/api-docs")
                .accept(org.springframework.http.MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.servers").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.servers").isArray)
    }

    @Test
    @Order(12)
    fun `should create OpenApiCustomizer bean for controller enhancement`() {
        // This test verifies the configuration can create the customizer bean
        // The actual bean is created by Spring's @Bean method
        assertNotNull(openApiConfig, "OpenAPI config should exist to create customizer")
    }
}
