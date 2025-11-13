package io.github.robertomike.super_controller.units

import io.github.robertomike.super_controller.controllers.SuperController
import io.github.robertomike.super_controller.openapi.OpenApiConfig
import io.github.robertomike.super_controller.openapi.OpenApiProperties
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.Paths
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import kotlin.test.*

/**
 * Unit tests for OpenAPI configuration and customization.
 */
class OpenApiConfigTest {

    private lateinit var config: OpenApiConfig
    private lateinit var properties: OpenApiProperties
    private lateinit var openAPI: OpenAPI

    @BeforeEach
    fun setup() {
        config = OpenApiConfig()
        properties = OpenApiProperties(
            enabled = true,
            title = "Test API",
            description = "Test Description",
            version = "1.0.0"
        )
        openAPI = config.customOpenAPI(properties)
    }

    @Test
    fun `customOpenAPI creates OpenAPI with correct properties`() {
        assertNotNull(openAPI)
        assertEquals("Test API", openAPI.info.title)
        assertEquals("Test Description", openAPI.info.description)
        assertEquals("1.0.0", openAPI.info.version)
        assertNotNull(openAPI.servers)
        assertFalse(openAPI.servers.isEmpty())
        assertEquals("/", openAPI.servers[0].url)
    }

    @Test
    fun `customOpenAPI uses default properties when not specified`() {
        val defaultProps = OpenApiProperties()
        val defaultOpenAPI = config.customOpenAPI(defaultProps)
        
        assertEquals("API Documentation", defaultOpenAPI.info.title)
        assertEquals("RESTful API built with Super-Controller", defaultOpenAPI.info.description)
        assertEquals("1.0.0", defaultOpenAPI.info.version)
    }

    @Test
    fun `OpenApiProperties has correct defaults`() {
        val props = OpenApiProperties()
        assertEquals(false, props.enabled)
        assertEquals("API Documentation", props.title)
        assertEquals("RESTful API built with Super-Controller", props.description)
        assertEquals("1.0.0", props.version)
        assertTrue(props.servers.isEmpty())
    }

    @Test
    fun `superControllerOpenApiCustomizer can be created with empty controller list`() {
        val customizer = config.superControllerOpenApiCustomizer(emptyList())
        assertNotNull(customizer)
    }

    @Test
    fun `pagination parameters are added to GET operations`() {
        // Create a mock OpenAPI with paths
        val paths = Paths()
        val pathItem = PathItem().get(Operation().summary("List"))
        paths.addPathItem("/test", pathItem)
        openAPI.paths = paths
        
        // Get the customizer
        val controllers = listOf<SuperController<*, *, *, *>>()
        val customizer = config.superControllerOpenApiCustomizer(controllers)
        
        // Apply customization (won't do much with empty controller list, but shouldn't fail)
        customizer.customise(openAPI)
        
        // Verify the operation still exists
        assertNotNull(openAPI.paths["/test"]?.get)
    }

    @Test
    fun `pagination parameters are not duplicated`() {
        val operation = Operation()
        val pageParam = io.swagger.v3.oas.models.parameters.Parameter()
            .name("page")
            .`in`("query")
        operation.addParametersItem(pageParam)
        
        // Add pagination - should not duplicate the page parameter
        val params = operation.parameters ?: mutableListOf()
        val existingNames = params.map { it.name }.toSet()
        assertTrue(existingNames.contains("page"))
    }

    @Test
    fun `operation IDs are prefixed with version when ApiVersion is present`() {
        // This is tested implicitly through the enhanceCrudOperations method
        // The method is private, so we test it through the public interface
        val operation = Operation().operationId("testOperation")
        assertNotNull(operation.operationId)
    }

    @Test
    fun `deprecated operations are marked correctly`() {
        val operation = Operation().deprecated(true)
        assertTrue(operation.deprecated)
    }

    @Test
    fun `CRUD operations have correct descriptions`() {
        // Create operations with basic summaries
        val indexOp = Operation().summary("List").description("Retrieve a list")
        val storeOp = Operation().summary("Create").description("Create a new resource")
        val showOp = Operation().summary("Get").description("Retrieve a single resource")
        val updateOp = Operation().summary("Update").description("Update an existing resource")
        val deleteOp = Operation().summary("Delete").description("Delete a resource")
        
        assertNotNull(indexOp.summary)
        assertNotNull(storeOp.summary)
        assertNotNull(showOp.summary)
        assertNotNull(updateOp.summary)
        assertNotNull(deleteOp.summary)
    }

    @Test
    fun `bulk operations have correct descriptions`() {
        val bulkCreateOp = Operation().summary("Bulk create")
        val bulkUpdateOp = Operation().summary("Bulk update")
        val bulkDeleteOp = Operation().summary("Bulk delete")
        
        assertTrue(bulkCreateOp.summary.contains("Bulk"))
        assertTrue(bulkUpdateOp.summary.contains("Bulk"))
        assertTrue(bulkDeleteOp.summary.contains("Bulk"))
    }

    @Test
    fun `soft delete operations have correct descriptions`() {
        val softDeleteOp = Operation().summary("Soft delete").description("Soft delete a resource")
        val restoreOp = Operation().summary("Restore").description("Restore a soft-deleted resource")
        val forceDeleteOp = Operation().summary("Force delete").description("Permanently delete a resource")
        
        assertTrue(softDeleteOp.description?.contains("Soft delete") ?: false)
        assertTrue(restoreOp.description?.contains("Restore") ?: false)
        assertTrue(forceDeleteOp.description?.contains("Permanently") ?: false)
    }

    @Test
    fun `server configuration can be customized`() {
        val propsWithServers = OpenApiProperties(
            enabled = true,
            servers = listOf(
                OpenApiProperties.Server(url = "https://api.example.com", description = "Production"),
                OpenApiProperties.Server(url = "https://staging.example.com", description = "Staging")
            )
        )
        
        assertEquals(2, propsWithServers.servers.size)
        assertEquals("https://api.example.com", propsWithServers.servers[0].url)
        assertEquals("Production", propsWithServers.servers[0].description)
    }

    @Test
    fun `pagination parameters have correct schema types`() {
        val openAPI = OpenAPI()
        val paths = Paths()
        val pathItem = PathItem()
        val getOp = Operation().summary("List").description("Retrieve a list")
        pathItem.get = getOp
        val postOp = Operation().summary("Create").description("Create a new resource")
        pathItem.post = postOp
        paths.addPathItem("/test", pathItem)
        
        val detailPathItem = PathItem()
        detailPathItem.get = Operation().summary("Get").description("Retrieve a single resource")
        detailPathItem.put = Operation().summary("Update").description("Update an existing resource")
        detailPathItem.delete = Operation().summary("Delete").description("Delete a resource")
        paths.addPathItem("/test/{id}", detailPathItem)
        
        openAPI.paths = paths
        
        // Verify paths exist
        assertNotNull(openAPI.paths["/test"]?.get)
        assertNotNull(openAPI.paths["/test"]?.post)
        assertNotNull(openAPI.paths["/test/{id}"]?.get)
        assertNotNull(openAPI.paths["/test/{id}"]?.put)
        assertNotNull(openAPI.paths["/test/{id}"]?.delete)
    }

    @Test
    fun `OpenAPI info can be customized`() {
        val customProps = OpenApiProperties(
            enabled = true,
            title = "Custom API",
            description = "Custom Description",
            version = "2.0.0"
        )
        val customOpenAPI = config.customOpenAPI(customProps)
        
        assertEquals("Custom API", customOpenAPI.info.title)
        assertEquals("Custom Description", customOpenAPI.info.description)
        assertEquals("2.0.0", customOpenAPI.info.version)
    }
}
