package io.github.robertomike.super_controller.integration

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.robertomike.super_controller.BasicTest
import io.github.robertomike.super_controller.config.router.RouterConfig
import io.github.robertomike.super_controller.controllers.SuperController
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Integration tests that hit the real SpringDoc endpoint to verify the
 * OpenAPI specification generated for Super-Controller endpoints.
 */
class OpenApiDocumentationIntegrationTest : BasicTest() {

    @Autowired(required = false)
    private var routerConfig: RouterConfig? = null

    @Autowired(required = false)
    private var controllers: List<SuperController<*, *, *, *>>? = null

    private fun fetchApiDocs(): JsonNode {
        val mapper = ObjectMapper()
        val mvcResult = mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/v3/api-docs")
        )
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk)
            .andReturn()

        return mapper.readTree(mvcResult.response.contentAsString)
    }

    @Test
    fun `OpenAPI docs expose user endpoints`() {
        println("RouterConfig present: ${routerConfig != null}")
        println("Controllers count: ${controllers?.size ?: 0}")
        println("Controllers: ${controllers?.map { it::class.simpleName }}")
        
        val pathsNode = fetchApiDocs().path("paths")
        val documentedPaths = availablePaths(pathsNode)

        assertTrue(documentedPaths.contains("/api/users"), "Documented paths: $documentedPaths")
        assertTrue(documentedPaths.contains("/api/users/{id}"), "Documented paths: $documentedPaths")
    }

    @Test
    fun `OpenAPI docs include soft delete metadata`() {
        val pathsNode = fetchApiDocs().path("paths")
        val documentedPaths = availablePaths(pathsNode)
        val softDeleteOperation = pathsNode.path("/api/users/{id}/soft-delete").get("delete")

        assertNotNull(
            softDeleteOperation,
            "Soft delete path missing; documented paths: $documentedPaths"
        )
        assertEquals("Soft delete User", softDeleteOperation.path("summary").asText())
        assertTrue(
            softDeleteOperation.path("description").asText().contains("Soft delete a User"),
            "Soft delete description should explain behavior"
        )
        assertEquals("softDeleteUser", softDeleteOperation.path("operationId").asText())
        val tags = softDeleteOperation.path("tags").map { it.asText() }
        assertTrue(tags.contains("Users"), "Soft delete operation should reuse the Users tag")
    }

    @Test
    fun `OpenAPI docs include bulk operations`() {
        val pathsNode = fetchApiDocs().path("paths")
        val bulkPath = pathsNode.path("/api/users/bulk")

        // Bulk create
        val bulkCreateOperation = bulkPath.get("post")
        assertNotNull(bulkCreateOperation, "Bulk create operation should exist")
        assertEquals("Bulk create Users", bulkCreateOperation.path("summary").asText())
        assertEquals("Create multiple Users in a single request", bulkCreateOperation.path("description").asText())
        assertEquals("bulkCreateUser", bulkCreateOperation.path("operationId").asText())

        // Bulk update
        val bulkUpdateOperation = bulkPath.get("put")
        assertNotNull(bulkUpdateOperation, "Bulk update operation should exist")
        assertEquals("Bulk update Users", bulkUpdateOperation.path("summary").asText())
        assertEquals("Update multiple Users in a single request", bulkUpdateOperation.path("description").asText())
        assertEquals("bulkUpdateUser", bulkUpdateOperation.path("operationId").asText())

        // Bulk delete
        val bulkDeleteOperation = bulkPath.get("delete")
        assertNotNull(bulkDeleteOperation, "Bulk delete operation should exist")
        assertEquals("Bulk delete Users", bulkDeleteOperation.path("summary").asText())
        assertEquals("Delete multiple Users in a single request", bulkDeleteOperation.path("description").asText())
        assertEquals("bulkDeleteUser", bulkDeleteOperation.path("operationId").asText())
    }

    @Test
    fun `OpenAPI docs include restore operation`() {
        val pathsNode = fetchApiDocs().path("paths")
        val restoreOperation = pathsNode.path("/api/users/{id}/restore").get("put")

        assertNotNull(restoreOperation, "Restore operation should exist")
        assertEquals("Restore User", restoreOperation.path("summary").asText())
        assertTrue(
            restoreOperation.path("description").asText().contains("Restore a soft-deleted User"),
            "Restore description should explain it's for soft-deleted records"
        )
        assertEquals("restoreUser", restoreOperation.path("operationId").asText())
        val tags = restoreOperation.path("tags").map { it.asText() }
        assertTrue(tags.contains("Users"), "Restore operation should use Users tag")
    }

    @Test
    fun `OpenAPI docs include force delete operation`() {
        val pathsNode = fetchApiDocs().path("paths")
        val forceDeleteOperation = pathsNode.path("/api/users/{id}/force").get("delete")

        assertNotNull(forceDeleteOperation, "Force delete operation should exist")
        assertEquals("Force delete User", forceDeleteOperation.path("summary").asText())
        assertTrue(
            forceDeleteOperation.path("description").asText().contains("Permanently delete a User"),
            "Force delete description should warn about permanence"
        )
        assertTrue(
            forceDeleteOperation.path("description").asText().contains("cannot be undone"),
            "Force delete description should warn it cannot be undone"
        )
        assertEquals("forceDeleteUser", forceDeleteOperation.path("operationId").asText())
    }

    @Test
    fun `OpenAPI docs include pagination parameters for index operations`() {
        val pathsNode = fetchApiDocs().path("paths")
        val indexOperation = pathsNode.path("/api/users").get("get")

        assertNotNull(indexOperation, "Index operation should exist")

        val parameters = indexOperation.path("parameters")
        assertTrue(parameters.isArray, "Parameters should be an array")

        val paramNames = parameters.map { it.path("name").asText() }
        assertTrue(paramNames.contains("page"), "Should have page parameter")
        assertTrue(paramNames.contains("size"), "Should have size parameter")
        assertTrue(paramNames.contains("sort"), "Should have sort parameter")

        // Check page parameter details
        val pageParam = parameters.find { it.path("name").asText() == "page" }
        assertNotNull(pageParam, "Page parameter should exist")
        assertEquals("query", pageParam.path("in").asText())
        assertEquals("Page number (0-indexed)", pageParam.path("description").asText())
        assertEquals(false, pageParam.path("required").asBoolean())
        assertEquals(0, pageParam.path("schema").path("default").asInt())

        // Check size parameter details
        val sizeParam = parameters.find { it.path("name").asText() == "size" }
        assertNotNull(sizeParam, "Size parameter should exist")
        assertEquals("query", sizeParam.path("in").asText())
        assertEquals("Number of items per page", sizeParam.path("description").asText())
        assertEquals(20, sizeParam.path("schema").path("default").asInt())

        // Check sort parameter details
        val sortParam = parameters.find { it.path("name").asText() == "sort" }
        assertNotNull(sortParam, "Sort parameter should exist")
        assertEquals("query", sortParam.path("in").asText())
        assertTrue(
            sortParam.path("description").asText().contains("property(,asc|desc)"),
            "Sort description should explain format"
        )
    }

    @Test
    fun `OpenAPI docs do not include pagination parameters for non-index operations`() {
        val pathsNode = fetchApiDocs().path("paths")
        val showOperation = pathsNode.path("/api/users/{id}").get("get")

        assertNotNull(showOperation, "Show operation should exist")

        val parameters = showOperation.path("parameters")
        if (parameters.isArray) {
            val paramNames = parameters.map { it.path("name").asText() }
            assertTrue(!paramNames.contains("page"), "Show operation should not have page parameter")
            assertTrue(!paramNames.contains("size"), "Show operation should not have size parameter")
            assertTrue(!paramNames.contains("sort"), "Show operation should not have sort parameter")
        }
    }

    @Test
    fun `OpenAPI docs include correct operation IDs`() {
        val pathsNode = fetchApiDocs().path("paths")

        // CRUD operations
        assertEquals("indexUsers", pathsNode.path("/api/users").get("get").path("operationId").asText())
        assertEquals("createUser", pathsNode.path("/api/users").get("post").path("operationId").asText())
        assertEquals("getUser", pathsNode.path("/api/users/{id}").get("get").path("operationId").asText())
        assertEquals("updateUser", pathsNode.path("/api/users/{id}").get("put").path("operationId").asText())
        assertEquals("deleteUser", pathsNode.path("/api/users/{id}").get("delete").path("operationId").asText())
    }

    @Test
    fun `OpenAPI docs include correct tags`() {
        val pathsNode = fetchApiDocs().path("paths")

        // Check Users tag
        val indexOperation = pathsNode.path("/api/users").get("get")
        val tags = indexOperation.path("tags").map { it.asText() }
        assertTrue(tags.contains("Users"), "Index operation should have Users tag")

        // Check Orders tag
        val ordersOperation = pathsNode.path("/api/orders").get("get")
        val ordersTags = ordersOperation.path("tags").map { it.asText() }
        assertTrue(ordersTags.contains("Orders"), "Orders index operation should have Orders tag")
    }

    @Test
    fun `OpenAPI docs include all CRUD operations for orders`() {
        val pathsNode = fetchApiDocs().path("paths")
        val documentedPaths = availablePaths(pathsNode)

        assertTrue(documentedPaths.contains("/api/orders"), "Should have orders index path")
        assertTrue(documentedPaths.contains("/api/orders/{id}"), "Should have orders detail path")

        // Check all operations exist
        val ordersPath = pathsNode.path("/api/orders")
        assertNotNull(ordersPath.get("get"), "Should have GET /api/orders")
        assertNotNull(ordersPath.get("post"), "Should have POST /api/orders")

        val orderDetailPath = pathsNode.path("/api/orders/{id}")
        assertNotNull(orderDetailPath.get("get"), "Should have GET /api/orders/{id}")
        assertNotNull(orderDetailPath.get("put"), "Should have PUT /api/orders/{id}")
        assertNotNull(orderDetailPath.get("delete"), "Should have DELETE /api/orders/{id}")
    }

    @Test
    fun `OpenAPI docs have correct descriptions for CRUD operations`() {
        val pathsNode = fetchApiDocs().path("paths")

        // Index
        val indexOp = pathsNode.path("/api/users").get("get")
        assertEquals("List Users", indexOp.path("summary").asText())
        assertTrue(indexOp.path("description").asText().contains("Retrieve a list of Users"))

        // Store
        val storeOp = pathsNode.path("/api/users").get("post")
        assertEquals("Create User", storeOp.path("summary").asText())
        assertEquals("Create a new User", storeOp.path("description").asText())

        // Show
        val showOp = pathsNode.path("/api/users/{id}").get("get")
        assertEquals("Get User", showOp.path("summary").asText())
        assertTrue(showOp.path("description").asText().contains("Retrieve a single User by ID"))

        // Update
        val updateOp = pathsNode.path("/api/users/{id}").get("put")
        assertEquals("Update User", updateOp.path("summary").asText())
        assertEquals("Update an existing User", updateOp.path("description").asText())

        // Delete
        val deleteOp = pathsNode.path("/api/users/{id}").get("delete")
        assertEquals("Delete User", deleteOp.path("summary").asText())
        assertTrue(deleteOp.path("description").asText().contains("Delete a User by ID"))
    }

    private fun availablePaths(pathsNode: JsonNode): List<String> =
        pathsNode.fieldNames().asSequence().toList()
}
