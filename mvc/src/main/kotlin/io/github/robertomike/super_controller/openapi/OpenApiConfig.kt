package io.github.robertomike.super_controller.openapi

import io.github.robertomike.super_controller.annotations.SuperControllerOpenApi
import io.github.robertomike.super_controller.controllers.SuperController
import io.github.robertomike.super_controller.controllers.markers.BulkOperationsMarker
import io.github.robertomike.super_controller.controllers.markers.SoftDeletableMarker
import io.github.robertomike.super_controller.versioning.ApiVersion
import io.github.robertomike.super_controller.versioning.VersionStrategy
import io.github.robertomike.super_controller.versioning.VersioningProperties
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
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
open class OpenApiConfig(val properties: OpenApiProperties, val versionProperties: VersioningProperties?) {
    /**
     * Creates the base OpenAPI configuration.
     */
    @Bean
    open fun customOpenAPI(): OpenAPI {
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

        val tags = openApiDoc?.tags?.toList() ?: listOf(resourceName.replaceFirstChar { it.uppercase() })

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
        val indexPath = paths[baseUrl] ?: PathItem().also { paths.addPathItem(baseUrl, it) }
        val indexOperation = indexPath.get ?: Operation().also { indexPath.get = it }
        indexOperation.summary = "List $resourceName"
        indexOperation.description = "Retrieve a list of $resourceName with pagination support"
        indexOperation.tags = tags
        indexOperation.operationId = prefixWithVersion("index${resourceName.replaceFirstChar { it.uppercase() }}", apiVersion)
        addPaginationParameters(indexOperation)
        addVersioningStrategy(indexOperation)
        markDeprecatedIfNeeded(indexOperation, apiVersion)

        // Store operation (POST /resource)
        val storePath = paths[baseUrl] ?: PathItem().also { paths.addPathItem(baseUrl, it) }
        val storeOperation = storePath.post ?: Operation().also { storePath.post = it }
        storeOperation.summary = "Create $singularName"
        storeOperation.description = "Create a new $singularName"
        storeOperation.tags = tags
        storeOperation.operationId = prefixWithVersion("create${singularName.replaceFirstChar { it.uppercase() }}", apiVersion)
        addVersioningStrategy(storeOperation)
        markDeprecatedIfNeeded(storeOperation, apiVersion)

        // Show operation (GET /resource/{id})
        val showPath = paths["$baseUrl/{id}"] ?: PathItem().also { paths.addPathItem("$baseUrl/{id}", it) }
        val showOperation = showPath.get ?: Operation().also { showPath.get = it }
        showOperation.summary = "Get $singularName"
        showOperation.description = "Retrieve a single $singularName by ID"
        showOperation.tags = tags
        showOperation.operationId = prefixWithVersion("get${singularName.replaceFirstChar { it.uppercase() }}", apiVersion)
        addVersioningStrategy(showOperation)
        markDeprecatedIfNeeded(showOperation, apiVersion)

        // Update operation (PUT /resource/{id})
        val updatePath = paths["$baseUrl/{id}"] ?: PathItem().also { paths.addPathItem("$baseUrl/{id}", it) }
        val updateOperation = updatePath.put ?: Operation().also { updatePath.put = it }
        updateOperation.summary = "Update $singularName"
        updateOperation.description = "Update an existing $singularName"
        updateOperation.tags = tags
        updateOperation.operationId = prefixWithVersion("update${singularName.replaceFirstChar { it.uppercase() }}", apiVersion)
        addVersioningStrategy(updateOperation)
        markDeprecatedIfNeeded(updateOperation, apiVersion)

        // Delete operation (DELETE /resource/{id})
        val deletePath = paths["$baseUrl/{id}"] ?: PathItem().also { paths.addPathItem("$baseUrl/{id}", it) }
        val deleteOperation = deletePath.delete ?: Operation().also { deletePath.delete = it }
        deleteOperation.summary = "Delete $singularName"
        deleteOperation.description = "Delete a $singularName by ID"
        deleteOperation.tags = tags
        deleteOperation.operationId = prefixWithVersion("delete${singularName.replaceFirstChar { it.uppercase() }}", apiVersion)
        addVersioningStrategy(deleteOperation)
        markDeprecatedIfNeeded(deleteOperation, apiVersion)
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
        val bulkCreatePath = paths["$baseUrl/bulk"] ?: PathItem().also { paths.addPathItem("$baseUrl/bulk", it) }
        val bulkCreateOperation = bulkCreatePath.post ?: Operation().also { bulkCreatePath.post = it }
        bulkCreateOperation.summary = "Bulk create ${singularName}s"
        bulkCreateOperation.description = "Create multiple ${singularName}s in a single request"
        bulkCreateOperation.tags = tags
        bulkCreateOperation.operationId = prefixWithVersion("bulkCreate${singularName.replaceFirstChar { it.uppercase() }}", apiVersion)
        addVersioningStrategy(bulkCreateOperation)
        markDeprecatedIfNeeded(bulkCreateOperation, apiVersion)

        // Bulk update (PUT /resource/bulk)
        val bulkUpdatePath = paths["$baseUrl/bulk"] ?: PathItem().also { paths.addPathItem("$baseUrl/bulk", it) }
        val bulkUpdateOperation = bulkUpdatePath.put ?: Operation().also { bulkUpdatePath.put = it }
        bulkUpdateOperation.summary = "Bulk update ${singularName}s"
        bulkUpdateOperation.description = "Update multiple ${singularName}s in a single request"
        bulkUpdateOperation.tags = tags
        bulkUpdateOperation.operationId = prefixWithVersion("bulkUpdate${singularName.replaceFirstChar { it.uppercase() }}", apiVersion)
        addVersioningStrategy(bulkUpdateOperation)
        markDeprecatedIfNeeded(bulkUpdateOperation, apiVersion)

        // Bulk delete (DELETE /resource/bulk)
        val bulkDeletePath = paths["$baseUrl/bulk"] ?: PathItem().also { paths.addPathItem("$baseUrl/bulk", it) }
        val bulkDeleteOperation = bulkDeletePath.delete ?: Operation().also { bulkDeletePath.delete = it }
        bulkDeleteOperation.summary = "Bulk delete ${singularName}s"
        bulkDeleteOperation.description = "Delete multiple ${singularName}s in a single request"
        bulkDeleteOperation.tags = tags
        bulkDeleteOperation.operationId = prefixWithVersion("bulkDelete${singularName.replaceFirstChar { it.uppercase() }}", apiVersion)
        addVersioningStrategy(bulkDeleteOperation)
        markDeprecatedIfNeeded(bulkDeleteOperation, apiVersion)
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
        val softDeletePath = paths["$baseUrl/{id}/soft-delete"] ?: PathItem().also { paths.addPathItem("$baseUrl/{id}/soft-delete", it) }
        val softDeleteOperation = softDeletePath.delete ?: Operation().also { softDeletePath.delete = it }
        softDeleteOperation.summary = "Soft delete $singularName"
        softDeleteOperation.description = "Soft delete a $singularName (can be restored later)"
        softDeleteOperation.tags = tags
        softDeleteOperation.operationId = prefixWithVersion("softDelete${singularName.replaceFirstChar { it.uppercase() }}", apiVersion)
        addVersioningStrategy(softDeleteOperation)
        markDeprecatedIfNeeded(softDeleteOperation, apiVersion)

        // Restore (PUT /resource/{id}/restore)
        val restorePath = paths["$baseUrl/{id}/restore"] ?: PathItem().also { paths.addPathItem("$baseUrl/{id}/restore", it) }
        val restoreOperation = restorePath.put ?: Operation().also { restorePath.put = it }
        restoreOperation.summary = "Restore $singularName"
        restoreOperation.description = "Restore a soft-deleted $singularName"
        restoreOperation.tags = tags
        restoreOperation.operationId = prefixWithVersion("restore${singularName.replaceFirstChar { it.uppercase() }}", apiVersion)
        addVersioningStrategy(restoreOperation)
        markDeprecatedIfNeeded(restoreOperation, apiVersion)

        // Force delete (DELETE /resource/{id}/force)
        val forceDeletePath = paths["$baseUrl/{id}/force"] ?: PathItem().also { paths.addPathItem("$baseUrl/{id}/force", it) }
        val forceDeleteOperation = forceDeletePath.delete ?: Operation().also { forceDeletePath.delete = it }
        forceDeleteOperation.summary = "Force delete $singularName"
        forceDeleteOperation.description = "Permanently delete a $singularName (cannot be undone)"
        forceDeleteOperation.tags = tags
        forceDeleteOperation.operationId = prefixWithVersion("forceDelete${singularName.replaceFirstChar { it.uppercase() }}", apiVersion)
        addVersioningStrategy(forceDeleteOperation)
        markDeprecatedIfNeeded(forceDeleteOperation, apiVersion)
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
                    .description("Sort criteria in the format: (-)property. Default sort order is ascending.")
                    .required(false)
                    .schema(sortSchema)
            )
        }
    }

    /**
     * Adds versioning parameters based on the configured strategy.
     */
    private fun addVersioningStrategy(operation: Operation) {
        if (versionProperties?.enabled != true) return
        
        if (operation.parameters == null) {
            operation.parameters = mutableListOf()
        }

        val existingParamNames = operation.parameters.map { it.name }.toSet()

        when (versionProperties.strategy) {
            VersionStrategy.HEADER -> {
                if (versionProperties.headerName !in existingParamNames) {
                    val versionSchema = Schema<String>()
                    versionSchema.setType("string")
                    versionSchema.setDefault(versionProperties.defaultVersion)

                    operation.addParametersItem(
                        Parameter()
                            .name(versionProperties.headerName)
                            .`in`("header")
                            .description("API version to use for this request")
                            .required(false)
                            .schema(versionSchema)
                    )
                }
            }
            VersionStrategy.PARAMETER -> {
                if (versionProperties.paramName !in existingParamNames) {
                    val versionSchema = Schema<String>()
                    versionSchema.setType("string")
                    versionSchema.setDefault(versionProperties.defaultVersion)

                    operation.addParametersItem(
                        Parameter()
                            .name(versionProperties.paramName)
                            .`in`("query")
                            .description("API version to use for this request")
                            .required(false)
                            .schema(versionSchema)
                    )
                }
            }
            VersionStrategy.ACCEPT_HEADER -> {
                // Accept header documentation is handled by adding it to the description
                if (operation.description?.contains("Accept header") != true) {
                    val acceptExample = "${versionProperties.mediaTypePrefix}.${versionProperties.defaultVersion}+json"
                    val versionNote = "\n\nVersion can be specified in Accept header: `$acceptExample`"
                    operation.description = (operation.description ?: "") + versionNote
                }
            }
            VersionStrategy.URI -> {
            }
        }
    }

    /**
     * Prefixes an operation ID with the API version if present.
     */
    private fun prefixWithVersion(operationId: String, apiVersion: ApiVersion?): String {
        return if (apiVersion != null && versionProperties?.enabled ?: false) {
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
