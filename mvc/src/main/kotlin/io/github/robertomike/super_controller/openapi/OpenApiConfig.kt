package io.github.robertomike.super_controller.openapi

import io.github.robertomike.super_controller.annotations.SuperControllerOpenApi
import io.github.robertomike.super_controller.controllers.SuperController
import io.github.robertomike.super_controller.controllers.markers.BulkOperationsMarker
import io.github.robertomike.super_controller.controllers.markers.SoftDeletableMarker
import io.github.robertomike.super_controller.versioning.ApiVersion
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.servers.Server
import org.springdoc.core.customizers.OpenApiCustomizer
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Configuration class for OpenAPI documentation generation.
 *
 * Automatically configures Swagger/OpenAPI documentation for Super-Controller
 * endpoints, including:
 * - Standard CRUD operations (index, store, show, update, destroy)
 * - Bulk operations (bulkStore, bulkUpdate, bulkDelete)
 * - Soft delete operations (softDelete, restore, forceDelete)
 * - API versioning metadata
 * - Deprecation warnings
 *
 * The generated documentation is available at:
 * - OpenAPI JSON: `/v3/api-docs`
 * - Swagger UI: `/swagger-ui.html`
 */
@Configuration
@ConditionalOnClass(value = [OpenAPI::class])
@ConditionalOnProperty(
    prefix = "super-controller.openapi",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = false
)
open class OpenApiConfig {

    /**
     * Configuration properties bean for OpenAPI customization.
     */
    @Bean
    open fun openApiProperties(): OpenApiProperties {
        return OpenApiProperties()
    }

    /**
     * Creates the base OpenAPI configuration.
     */
    @Bean
    open fun customOpenAPI(properties: OpenApiProperties): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title(properties.title)
                    .version(properties.version)
                    .description(properties.description)
            )
            .servers(listOf(Server().url("/").description("Default Server")))
    }

    /**
     * Customizer that enhances the OpenAPI specification with Super-Controller specific metadata.
     */
    @Bean
    open fun superControllerOpenApiCustomizer(
        controllers: List<SuperController<*, *, *, *>>
    ): OpenApiCustomizer {
        return OpenApiCustomizer { openApi ->
            controllers.forEach { controller ->
                enhanceControllerPaths(openApi, controller)
            }
        }
    }

    /**
     * Enhances OpenAPI paths with metadata from Super-Controller annotations.
     */
    private fun enhanceControllerPaths(openApi: OpenAPI, controller: SuperController<*, *, *, *>) {
        val baseUrl = controller.baseUrl
        val controllerClass = controller::class.java

        // Get controller annotations
        val apiVersion = controllerClass.getAnnotation(ApiVersion::class.java)
        val openApiDoc = controllerClass.getAnnotation(SuperControllerOpenApi::class.java)

        // Determine the resource name from the controller's path
        val resourceName = controller.path ?: "resources"
        val singularName = if (resourceName.endsWith("s")) {
            resourceName.dropLast(1)
        } else {
            resourceName
        }

        val tags = openApiDoc?.tags?.toList() ?: listOf(resourceName.capitalize())

        // Enhance standard CRUD operations
        enhanceCrudOperations(openApi, baseUrl, singularName, resourceName, tags, apiVersion)

        // Enhance bulk operations if controller supports them
        if (controller is BulkOperationsMarker<*, *, *, *>) {
            enhanceBulkOperations(openApi, baseUrl, singularName, tags, apiVersion)
        }

        // Enhance soft delete operations if controller supports them
        if (controller is SoftDeletableMarker<*, *>) {
            enhanceSoftDeleteOperations(openApi, baseUrl, singularName, tags, apiVersion)
        }
    }

    /**
     * Enhances standard CRUD operations in the OpenAPI specification.
     */
    private fun enhanceCrudOperations(
        openApi: OpenAPI,
        baseUrl: String,
        singularName: String,
        resourceName: String,
        tags: List<String>,
        apiVersion: ApiVersion?
    ) {
        val paths = openApi.paths ?: return

        // Index operation (GET /resource)
        paths[baseUrl]?.get?.let { operation ->
            operation.summary = "List $resourceName"
            operation.description = "Retrieve a list of $resourceName with pagination support"
            operation.tags = tags
            operation.operationId = prefixWithVersion("index${resourceName.capitalize()}", apiVersion)
            addPaginationParameters(operation)
            markDeprecatedIfNeeded(operation, apiVersion)
        }

        // Store operation (POST /resource)
        paths[baseUrl]?.post?.let { operation ->
            operation.summary = "Create $singularName"
            operation.description = "Create a new $singularName"
            operation.tags = tags
            operation.operationId = prefixWithVersion("create${singularName.capitalize()}", apiVersion)
            markDeprecatedIfNeeded(operation, apiVersion)
        }

        // Show operation (GET /resource/{id})
        paths["$baseUrl/{id}"]?.get?.let { operation ->
            operation.summary = "Get $singularName"
            operation.description = "Retrieve a single $singularName by ID"
            operation.tags = tags
            operation.operationId = prefixWithVersion("get${singularName.capitalize()}", apiVersion)
            markDeprecatedIfNeeded(operation, apiVersion)
        }

        // Update operation (PUT /resource/{id})
        paths["$baseUrl/{id}"]?.put?.let { operation ->
            operation.summary = "Update $singularName"
            operation.description = "Update an existing $singularName"
            operation.tags = tags
            operation.operationId = prefixWithVersion("update${singularName.capitalize()}", apiVersion)
            markDeprecatedIfNeeded(operation, apiVersion)
        }

        // Delete operation (DELETE /resource/{id})
        paths["$baseUrl/{id}"]?.delete?.let { operation ->
            operation.summary = "Delete $singularName"
            operation.description = "Delete a $singularName by ID"
            operation.tags = tags
            operation.operationId = prefixWithVersion("delete${singularName.capitalize()}", apiVersion)
            markDeprecatedIfNeeded(operation, apiVersion)
        }
    }

    /**
     * Enhances bulk operations in the OpenAPI specification.
     */
    private fun enhanceBulkOperations(
        openApi: OpenAPI,
        baseUrl: String,
        singularName: String,
        tags: List<String>,
        apiVersion: ApiVersion?
    ) {
        val paths = openApi.paths ?: return

        // Bulk create (POST /resource/bulk)
        paths["$baseUrl/bulk"]?.post?.let { operation ->
            operation.summary = "Bulk create ${singularName}s"
            operation.description = "Create multiple ${singularName}s in a single request"
            operation.tags = tags
            operation.operationId = prefixWithVersion("bulkCreate${singularName.capitalize()}", apiVersion)
            markDeprecatedIfNeeded(operation, apiVersion)
        }

        // Bulk update (PUT /resource/bulk)
        paths["$baseUrl/bulk"]?.put?.let { operation ->
            operation.summary = "Bulk update ${singularName}s"
            operation.description = "Update multiple ${singularName}s in a single request"
            operation.tags = tags
            operation.operationId = prefixWithVersion("bulkUpdate${singularName.capitalize()}", apiVersion)
            markDeprecatedIfNeeded(operation, apiVersion)
        }

        // Bulk delete (DELETE /resource/bulk)
        paths["$baseUrl/bulk"]?.delete?.let { operation ->
            operation.summary = "Bulk delete ${singularName}s"
            operation.description = "Delete multiple ${singularName}s in a single request"
            operation.tags = tags
            operation.operationId = prefixWithVersion("bulkDelete${singularName.capitalize()}", apiVersion)
            markDeprecatedIfNeeded(operation, apiVersion)
        }
    }

    /**
     * Enhances soft delete operations in the OpenAPI specification.
     */
    private fun enhanceSoftDeleteOperations(
        openApi: OpenAPI,
        baseUrl: String,
        singularName: String,
        tags: List<String>,
        apiVersion: ApiVersion?
    ) {
        val paths = openApi.paths ?: return

        // Soft delete (DELETE /resource/{id}/soft-delete)
        paths["$baseUrl/{id}/soft-delete"]?.delete?.let { operation ->
            operation.summary = "Soft delete $singularName"
            operation.description = "Soft delete a $singularName (can be restored later)"
            operation.tags = tags
            operation.operationId = prefixWithVersion("softDelete${singularName.capitalize()}", apiVersion)
            markDeprecatedIfNeeded(operation, apiVersion)
        }

        // Restore (PUT /resource/{id}/restore)
        paths["$baseUrl/{id}/restore"]?.put?.let { operation ->
            operation.summary = "Restore $singularName"
            operation.description = "Restore a soft-deleted $singularName"
            operation.tags = tags
            operation.operationId = prefixWithVersion("restore${singularName.capitalize()}", apiVersion)
            markDeprecatedIfNeeded(operation, apiVersion)
        }

        // Force delete (DELETE /resource/{id}/force)
        paths["$baseUrl/{id}/force"]?.delete?.let { operation ->
            operation.summary = "Force delete $singularName"
            operation.description = "Permanently delete a $singularName (cannot be undone)"
            operation.tags = tags
            operation.operationId = prefixWithVersion("forceDelete${singularName.capitalize()}", apiVersion)
            markDeprecatedIfNeeded(operation, apiVersion)
        }
    }

    /**
     * Adds pagination parameters to an operation.
     */
    private fun addPaginationParameters(operation: Operation) {
        if (operation.parameters == null) {
            operation.parameters = mutableListOf()
        }

        // Only add pagination parameters to index operations (GET without path parameters)
        val existingParamNames = operation.parameters.map { it.name }.toSet()

        if ("page" !in existingParamNames) {
            val pageSchema = Schema<Int>()
            pageSchema.setType("integer")
            pageSchema.setDefault(0)

            operation.addParametersItem(
                Parameter()
                    .name("page")
                    .`in`("query")
                    .description("Page number (0-indexed)")
                    .required(false)
                    .schema(pageSchema)
            )
        }

        if ("size" !in existingParamNames) {
            val sizeSchema = Schema<Int>()
            sizeSchema.setType("integer")
            sizeSchema.setDefault(20)

            operation.addParametersItem(
                Parameter()
                    .name("size")
                    .`in`("query")
                    .description("Number of items per page")
                    .required(false)
                    .schema(sizeSchema)
            )
        }

        if ("sort" !in existingParamNames) {
            val sortSchema = Schema<String>()
            sortSchema.setType("string")

            operation.addParametersItem(
                Parameter()
                    .name("sort")
                    .`in`("query")
                    .description("Sort criteria in the format: property(,asc|desc). Default sort order is ascending.")
                    .required(false)
                    .schema(sortSchema)
            )
        }
    }

    /**
     * Prefixes an operation ID with the API version if present.
     */
    private fun prefixWithVersion(operationId: String, apiVersion: ApiVersion?): String {
        return if (apiVersion != null) {
            "${apiVersion.value}_$operationId"
        } else {
            operationId
        }
    }

    /**
     * Marks an operation as deprecated if the API version indicates it.
     */
    private fun markDeprecatedIfNeeded(operation: Operation, apiVersion: ApiVersion?) {
        if (apiVersion?.deprecated == true) {
            operation.deprecated = true

            val deprecationNotice = buildString {
                append("⚠️ DEPRECATED: This endpoint is deprecated")
                if (apiVersion.sunset.isNotEmpty()) {
                    append(" and will be removed on ${apiVersion.sunset}")
                }
                if (apiVersion.documentationUrl.isNotEmpty()) {
                    append(". See migration guide: ${apiVersion.documentationUrl}")
                }
            }

            operation.description = if (operation.description != null) {
                "$deprecationNotice\n\n${operation.description}"
            } else {
                deprecationNotice
            }
        }
    }
}
