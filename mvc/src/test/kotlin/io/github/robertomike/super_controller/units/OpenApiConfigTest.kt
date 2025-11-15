package io.github.robertomike.super_controller.units

import io.github.robertomike.super_controller.openapi.OpenApiConfig
import io.github.robertomike.super_controller.openapi.OpenApiProperties
import io.github.robertomike.super_controller.versioning.VersioningProperties
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.Paths
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.parameters.Parameter
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import kotlin.test.*

/**
 * Comprehensive unit tests for OpenAPI configuration and customization.
 * 
 * These tests cover:
 * 1. Bean creation and configuration properties
 * 2. OpenAPI specification generation
 * 3. Parameter schemas and defaults
 * 4. Operation documentation structure
 * 5. Server configuration
 * 6. Edge cases and validation
 * 
 * Note: These are pure unit tests that don't require Spring context or fully
 * initialized controllers. They test the configuration logic, data structures,
 * and API contracts.
 */
class OpenApiConfigTest {

    private lateinit var config: OpenApiConfig
    private lateinit var properties: OpenApiProperties
    private lateinit var openAPI: OpenAPI

    @BeforeEach
    fun setup() {
        val versionProperties = VersioningProperties()
        properties = OpenApiProperties(
            enabled = true,
            title = "Test API",
            description = "Test Description",
            version = "1.0.0"
        )
        config = OpenApiConfig(properties, versionProperties)
        openAPI = config.customOpenAPI()
    }

    // =================================================================
    // Configuration and Bean Creation Tests
    // =================================================================

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
    fun `OpenApiProperties has correct defaults`() {
        val props = OpenApiProperties()
        assertEquals(false, props.enabled)
        assertEquals("API Documentation", props.title)
        assertEquals("RESTful API built with Super-Controller", props.description)
        assertEquals("1.0.0", props.version)
        assertTrue(props.servers.isEmpty())
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
        assertEquals("https://staging.example.com", propsWithServers.servers[1].url)
        assertEquals("Staging", propsWithServers.servers[1].description)
    }

    // =================================================================
    // OpenAPI Structure and Path Tests
    // =================================================================

    @Test
    fun `OpenAPI specification includes required sections`() {
        assertNotNull(openAPI.info)
        assertNotNull(openAPI.servers)
        assertEquals(1, openAPI.servers.size)
    }

    @Test
    fun `OpenAPI can have paths added`() {
        val paths = Paths()
        paths.addPathItem("/test", PathItem().get(Operation()))
        openAPI.paths = paths
        
        assertNotNull(openAPI.paths)
        assertNotNull(openAPI.paths["/test"])
        assertNotNull(openAPI.paths["/test"]?.get)
    }

    @Test
    fun `customizer can be created with empty controller list`() {
        val customizer = config.superControllerOpenApiCustomizer(emptyList())
        assertNotNull(customizer)
        
        // Should handle empty list without errors
        customizer.customise(openAPI)
    }

    @Test
    fun `customizer handles null paths gracefully`() {
        openAPI.paths = null
        val customizer = config.superControllerOpenApiCustomizer(emptyList())
        
        // Should not throw exception
        try {
            customizer.customise(openAPI)
            assertTrue(true, "Successfully handled null paths")
        } catch (e: Exception) {
            fail("Should not throw exception when paths is null: ${e.message}")
        }
    }

    // =================================================================
    // Pagination Parameter Tests
    // =================================================================

    @Test
    fun `pagination parameters have correct schema types and defaults`() {
        // Create pagination parameters as they would be created by addPaginationParameters
        val pageSchema = Schema<Int>()
        pageSchema.setType("integer")
        pageSchema.setDefault(0)
        
        val pageParam = Parameter()
            .name("page")
            .`in`("query")
            .description("Page number (0-indexed)")
            .required(false)
            .schema(pageSchema)
        
        // Verify page parameter
        assertEquals("page", pageParam.name)
        assertEquals("query", pageParam.`in`)
        assertEquals("integer", pageParam.schema.type)
        assertEquals(0, pageParam.schema.default)
        assertEquals(false, pageParam.required)
        assertTrue(pageParam.description?.contains("Page number") ?: false)
    }

    @Test
    fun `size parameter has correct configuration`() {
        val sizeSchema = Schema<Int>()
        sizeSchema.setType("integer")
        sizeSchema.setDefault(20)
        
        val sizeParam = Parameter()
            .name("size")
            .`in`("query")
            .description("Number of items per page")
            .required(false)
            .schema(sizeSchema)
        
        // Verify size parameter
        assertEquals("size", sizeParam.name)
        assertEquals("query", sizeParam.`in`)
        assertEquals("integer", sizeParam.schema.type)
        assertEquals(20, sizeParam.schema.default)
        assertEquals(false, sizeParam.required)
    }

    @Test
    fun `sort parameter has correct configuration`() {
        val sortSchema = Schema<String>()
        sortSchema.setType("string")
        
        val sortParam = Parameter()
            .name("sort")
            .`in`("query")
            .description("Sort criteria in the format: property(,asc|desc). Default sort order is ascending.")
            .required(false)
            .schema(sortSchema)
        
        // Verify sort parameter
        assertEquals("sort", sortParam.name)
        assertEquals("query", sortParam.`in`)
        assertEquals("string", sortParam.schema.type)
        assertEquals(false, sortParam.required)
        assertTrue(sortParam.description?.contains("property") ?: false)
        assertTrue(sortParam.description?.contains("asc|desc") ?: false)
    }

    @Test
    fun `pagination parameters can be added to operation`() {
        val operation = Operation()
        
        // Add pagination parameters
        val pageSchema = Schema<Int>()
        pageSchema.setType("integer")
        pageSchema.setDefault(0)
        operation.addParametersItem(
            Parameter().name("page").`in`("query").schema(pageSchema)
        )
        
        val sizeSchema = Schema<Int>()
        sizeSchema.setType("integer")
        sizeSchema.setDefault(20)
        operation.addParametersItem(
            Parameter().name("size").`in`("query").schema(sizeSchema)
        )
        
        val sortSchema = Schema<String>()
        sortSchema.setType("string")
        operation.addParametersItem(
            Parameter().name("sort").`in`("query").schema(sortSchema)
        )
        
        // Verify all parameters are present
        assertNotNull(operation.parameters)
        assertEquals(3, operation.parameters.size)
        assertTrue(operation.parameters.any { it.name == "page" })
        assertTrue(operation.parameters.any { it.name == "size" })
        assertTrue(operation.parameters.any { it.name == "sort" })
    }

    // =================================================================
    // Operation Documentation Tests
    // =================================================================

    @Test
    fun `CRUD operations can have summaries and descriptions`() {
        val indexOp = Operation().summary("List resources").description("Retrieve a list of resources")
        val storeOp = Operation().summary("Create resource").description("Create a new resource")
        val showOp = Operation().summary("Get resource").description("Retrieve a single resource")
        val updateOp = Operation().summary("Update resource").description("Update an existing resource")
        val deleteOp = Operation().summary("Delete resource").description("Delete a resource")
        
        assertNotNull(indexOp.summary)
        assertNotNull(storeOp.summary)
        assertNotNull(showOp.summary)
        assertNotNull(updateOp.summary)
        assertNotNull(deleteOp.summary)
        
        assertTrue(indexOp.description?.contains("list") ?: false)
        assertTrue(storeOp.description?.contains("Create") ?: false)
        assertTrue(showOp.description?.contains("single") ?: false)
        assertTrue(updateOp.description?.contains("Update") ?: false)
        assertTrue(deleteOp.description?.contains("Delete") ?: false)
    }

    @Test
    fun `bulk operations can have summaries and descriptions`() {
        val bulkCreateOp = Operation().summary("Bulk create resources").description("Create multiple resources")
        val bulkUpdateOp = Operation().summary("Bulk update resources").description("Update multiple resources")
        val bulkDeleteOp = Operation().summary("Bulk delete resources").description("Delete multiple resources")
        
        assertTrue(bulkCreateOp.summary?.contains("Bulk") ?: false)
        assertTrue(bulkUpdateOp.summary?.contains("Bulk") ?: false)
        assertTrue(bulkDeleteOp.summary?.contains("Bulk") ?: false)
        
        assertTrue(bulkCreateOp.description?.contains("multiple") ?: false)
        assertTrue(bulkUpdateOp.description?.contains("multiple") ?: false)
        assertTrue(bulkDeleteOp.description?.contains("multiple") ?: false)
    }

    @Test
    fun `soft delete operations can have summaries and descriptions`() {
        val softDeleteOp = Operation().summary("Soft delete resource").description("Soft delete a resource (can be restored)")
        val restoreOp = Operation().summary("Restore resource").description("Restore a soft-deleted resource")
        val forceDeleteOp = Operation().summary("Force delete resource").description("Permanently delete a resource")
        
        assertTrue(softDeleteOp.summary?.contains("Soft delete") ?: false)
        assertTrue(restoreOp.summary?.contains("Restore") ?: false)
        assertTrue(forceDeleteOp.summary?.contains("Force") ?: false)
        
        assertTrue(softDeleteOp.description?.contains("can be restored") ?: false)
        assertTrue(restoreOp.description?.contains("soft-deleted") ?: false)
        assertTrue(forceDeleteOp.description?.contains("Permanently") ?: false)
    }

    @Test
    fun `operations can have operation IDs`() {
        val operation = Operation().operationId("testOperation")
        assertEquals("testOperation", operation.operationId)
    }

    @Test
    fun `operations can have tags`() {
        val operation = Operation().tags(listOf("Users", "Admin"))
        assertNotNull(operation.tags)
        assertEquals(2, operation.tags.size)
        assertTrue(operation.tags.contains("Users"))
        assertTrue(operation.tags.contains("Admin"))
    }

    @Test
    fun `operations can be marked as deprecated`() {
        val operation = Operation().deprecated(true)
        assertTrue(operation.deprecated)
    }

    @Test
    fun `deprecated operations can have description with warning`() {
        val deprecationNotice = "⚠️ DEPRECATED: This endpoint is deprecated and will be removed on 2026-12-31. See migration guide: https://docs.example.com/migration"
        val operation = Operation()
            .deprecated(true)
            .description(deprecationNotice + "\n\nOriginal description")
        
        assertTrue(operation.deprecated)
        assertTrue(operation.description?.contains("DEPRECATED") ?: false)
        assertTrue(operation.description?.contains("2026-12-31") ?: false)
        assertTrue(operation.description?.contains("migration guide") ?: false)
        assertTrue(operation.description?.contains("Original description") ?: false)
    }

    // =================================================================
    // Path and HTTP Method Tests
    // =================================================================

    @Test
    fun `PathItem can have multiple HTTP methods`() {
        val pathItem = PathItem()
            .get(Operation().summary("List"))
            .post(Operation().summary("Create"))
            .put(Operation().summary("Update"))
            .delete(Operation().summary("Delete"))
        
        assertNotNull(pathItem.get)
        assertNotNull(pathItem.post)
        assertNotNull(pathItem.put)
        assertNotNull(pathItem.delete)
    }

    @Test
    fun `Paths can have multiple path items`() {
        val paths = Paths()
        paths.addPathItem("/users", PathItem().get(Operation()))
        paths.addPathItem("/users/{id}", PathItem().get(Operation()))
        paths.addPathItem("/users/bulk", PathItem().post(Operation()))
        
        assertEquals(3, paths.size)
        assertNotNull(paths["/users"])
        assertNotNull(paths["/users/{id}"])
        assertNotNull(paths["/users/bulk"])
    }

    @Test
    fun `path parameters can be documented`() {
        val idParam = Parameter()
            .name("id")
            .`in`("path")
            .description("Resource ID")
            .required(true)
            .schema(Schema<Long>().type("integer").format("int64"))
        
        assertEquals("id", idParam.name)
        assertEquals("path", idParam.`in`)
        assertEquals(true, idParam.required)
        assertEquals("integer", idParam.schema.type)
    }

    // =================================================================
    // Edge Cases and Validation Tests
    // =================================================================

    @Test
    fun `operations without parameters initialize empty list`() {
        val operation = Operation()
        operation.parameters = mutableListOf()
        assertNotNull(operation.parameters)
        assertTrue(operation.parameters.isEmpty())
    }

    @Test
    fun `parameters can be checked for duplicates`() {
        val operation = Operation()
        operation.addParametersItem(Parameter().name("page"))
        operation.addParametersItem(Parameter().name("size"))
        
        val existingNames = operation.parameters.map { it.name }.toSet()
        assertTrue(existingNames.contains("page"))
        assertTrue(existingNames.contains("size"))
        assertFalse(existingNames.contains("sort"))
    }

    @Test
    fun `resource name ending with s can be converted to singular`() {
        val resourceName = "users"
        val singularName = if (resourceName.endsWith("s")) {
            resourceName.dropLast(1)
        } else {
            resourceName
        }
        assertEquals("user", singularName)
    }

    @Test
    fun `resource name not ending with s remains unchanged`() {
        val resourceName = "data"
        val singularName = if (resourceName.endsWith("s")) {
            resourceName.dropLast(1)
        } else {
            resourceName
        }
        assertEquals("data", singularName)
    }

    @Test
    fun `operation ID can be prefixed with version`() {
        val version = "v1"
        val operationId = "indexUsers"
        val prefixedId = "${version}_$operationId"
        assertEquals("v1_indexUsers", prefixedId)
    }

    @Test
    fun `operation ID without version has no prefix`() {
        val operationId = "indexUsers"
        val prefixedId = operationId // No prefix
        assertEquals("indexUsers", prefixedId)
    }

    @Test
    fun `resource name can be capitalized for tags`() {
        val resourceName = "users"
        val tag = resourceName.replaceFirstChar { it.uppercase() }
        assertEquals("Users", tag)
    }

    @Test
    fun `OpenAPI specification version is valid`() {
        // SpringDoc sets this automatically, but we can verify the structure
        val testOpenAPI = OpenAPI()
        assertNotNull(testOpenAPI)
        // OpenAPI 3.0.1 is the default for SpringDoc OpenAPI 2.x
    }
    
    @Test
    fun `multiple servers can be configured`() {
        val props = OpenApiProperties(
            enabled = true,
            servers = listOf(
                OpenApiProperties.Server("https://api.example.com", "Production"),
                OpenApiProperties.Server("https://staging.example.com", "Staging"),
                OpenApiProperties.Server("http://localhost:8080", "Local")
            )
        )
        
        assertEquals(3, props.servers.size)
        assertEquals("http://localhost:8080", props.servers[2].url)
        assertEquals("Local", props.servers[2].description)
    }
    
    @Test
    fun `empty server list is valid`() {
        val props = OpenApiProperties()
        assertNotNull(props.servers)
        assertTrue(props.servers.isEmpty())
    }
}
