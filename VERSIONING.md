# API Versioning

Super-Controller provides comprehensive API versioning support with multiple strategies to suit different architectural needs.

## Table of Contents

- [Features](#features)
- [Quick Start](#quick-start)
- [Versioning Strategies](#versioning-strategies)
- [Configuration](#configuration)
- [Usage Examples](#usage-examples)
- [Best Practices](#best-practices)

## Features

- **Multiple Versioning Strategies**: URI, Header, Query Parameter, and Accept Header
- **Auto-Configuration**: Automatically enabled with Spring Boot configuration
- **Version Routing**: Custom request conditions for intelligent routing
- **Deprecation Support**: Add deprecation headers to old API versions
- **Backwards Compatible**: Works seamlessly with existing controllers
- **Type-Safe**: Kotlin-first with full type safety

## Quick Start

### 1. Enable Versioning

Add to your `application.properties` or `application.yml`:

```properties
super-controller.versioning.enabled=true
super-controller.versioning.strategy=URI
super-controller.versioning.default-version=v1
```

### 2. Annotate Your Controllers

```kotlin
@ApiVersion("v1")
@RestController
class UserControllerV1(
    userService: UserService,
    override val mapper: UserResponseMapper
) : SuperController<User, Long, StoreUserRequest, UpdateUserRequest>(userService)
```

### 3. Make Requests

Based on your chosen strategy:

- **URI**: `GET /v1/api/users`
- **HEADER**: `GET /api/users` with header `X-API-Version: v1`
- **PARAMETER**: `GET /api/users?version=v1`
- **ACCEPT_HEADER**: `GET /api/users` with header `Accept: application/vnd.api.v1+json`

## Versioning Strategies

### URI Strategy

Version is part of the URL path.

**Configuration:**

```properties
super-controller.versioning.strategy=URI
```

**Example:**

```kotlin
@ApiVersion("v1")
@RestController
class UserControllerV1 : SuperController<...>()

@ApiVersion("v2")
@RestController
class UserControllerV2 : SuperController<...>()
```

**Requests:**

```bash
# Version 1
GET /v1/api/users

# Version 2
GET /v2/api/users
```

**Pros:**

- Easy to test and debug
- Clear versioning in URLs
- Cacheable

**Cons:**

- Version appears in all URLs
- URL changes when version changes

### Header Strategy

Version is specified in a custom HTTP header.

**Configuration:**

```properties
super-controller.versioning.strategy=HEADER
super-controller.versioning.header-name=X-API-Version
```

**Example:**

```kotlin
@ApiVersion("v1")
@RestController
class UserControllerV1 : SuperController<...>()
```

**Requests:**

```bash
GET /api/users
X-API-Version: v1
```

**Pros:**

- Clean URLs
- Easy to add to all requests via interceptor
- Version doesn't clutter URL

**Cons:**

- Not visible in browser URL
- Requires custom headers

### Parameter Strategy

Version is specified as a query parameter.

**Configuration:**

```properties
super-controller.versioning.strategy=PARAMETER
super-controller.versioning.param-name=version
```

**Example:**

```kotlin
@ApiVersion("v1")
@RestController
class UserControllerV1 : SuperController<...>()
```

**Requests:**

```bash
GET /api/users?version=v1
```

**Pros:**

- Easy to test in browser
- No special headers needed
- Visible in URL

**Cons:**

- Version parameter in all URLs
- Can conflict with other query parameters

### Accept Header Strategy

Version is specified in the Accept header using media type versioning.

**Configuration:**

```properties
super-controller.versioning.strategy=ACCEPT_HEADER
super-controller.versioning.media-type-prefix=application/vnd.api
```

**Example:**

```kotlin
@ApiVersion("v1")
@RestController
class UserControllerV1 : SuperController<...>()
```

**Requests:**

```bash
GET /api/users
Accept: application/vnd.api.v1+json
```

**Pros:**

- RESTful standard approach
- Clean URLs
- Follows HTTP semantics

**Cons:**

- More complex to test
- Requires understanding of media types

## Configuration

### Full Configuration Options

```properties
# Enable versioning (default: false)
super-controller.versioning.enabled=true

# Versioning strategy (URI, HEADER, PARAMETER, ACCEPT_HEADER)
super-controller.versioning.strategy=URI

# Default version when none specified (default: v1)
super-controller.versioning.default-version=v1

# Custom header name for HEADER strategy (default: X-API-Version)
super-controller.versioning.header-name=X-API-Version

# Custom parameter name for PARAMETER strategy (default: version)
super-controller.versioning.param-name=version

# Media type prefix for ACCEPT_HEADER strategy (default: application/vnd.api)
super-controller.versioning.media-type-prefix=application/vnd.api

# Add deprecation headers (default: false)
super-controller.versioning.add-deprecation-headers=true
```

### Disabling Versioning

```properties
super-controller.versioning.enabled=false
```

When disabled, all controllers work normally without version routing.

## Usage Examples

### Basic Versioning

```kotlin
// Version 1
@ApiVersion("v1")
@RestController
class UserControllerV1(
    userService: UserService,
    override val mapper: UserResponseMapper
) : SuperController<User, Long, StoreUserRequest, UpdateUserRequest>(userService)

// Version 2 with new features
@ApiVersion("v2")
@RestController
class UserControllerV2(
    userService: UserServiceV2,
    override val mapper: UserResponseMapperV2
) : SuperController<User, Long, StoreUserRequestV2, UpdateUserRequestV2>(userService) {

    @PostMapping("/bulk-import")
    fun bulkImport(@RequestBody users: List<StoreUserRequestV2>): ResponseEntity<List<User>> {
        // New feature in v2
        return ResponseEntity.ok(userService.bulkImport(users))
    }
}
```

### Deprecating Old Versions

```kotlin
@ApiVersion("v1", deprecated = true)
@RestController
class UserControllerV1 : SuperController<...>()
```

With `add-deprecation-headers=true`, responses will include:

```
X-API-Deprecated: true
X-API-Deprecation-Info: This API version is deprecated. Please migrate to v2.
```

### Version-Specific Behavior

```kotlin
@ApiVersion("v2")
@RestController
class ProductControllerV2(
    productService: ProductService,
    override val mapper: ProductResponseMapper
) : SuperController<Product, Long, StoreProductRequest, UpdateProductRequest>(productService) {

    override fun beforeCreate(request: StoreProductRequest): StoreProductRequest {
        // V2-specific validation
        if (request.price < 0) {
            throw IllegalArgumentException("Price must be positive in API v2")
        }
        return request
    }
}
```

### Testing Versioned APIs

```kotlin
@TestPropertySource(properties = [
    "super-controller.versioning.enabled=true",
    "super-controller.versioning.strategy=HEADER",
    "super-controller.versioning.default-version=v1"
])
class UserVersioningTest : BasicTest() {

    @Test
    fun `should route to v1 with version header`() {
        mockMvc.perform(
            get("/api/users")
                .header("X-API-Version", "v1")
        )
            .andExpect(status().isOk)
            .andExpect(header().string("X-API-Version", "v1"))
    }

    @Test
    fun `should route to v2 with version header`() {
        mockMvc.perform(
            get("/api/users")
                .header("X-API-Version", "v2")
        )
            .andExpect(status().isOk)
            .andExpect(header().string("X-API-Version", "v2"))
    }
}
```

## Best Practices

### 1. Choose the Right Strategy

- **URI**: Best for public APIs, easy discoverability
- **HEADER**: Best for internal APIs, clean URLs
- **PARAMETER**: Good for gradual migration, browser testing
- **ACCEPT_HEADER**: Most RESTful, follows HTTP standards

### 2. Version Early

Start versioning from v1, even if you don't plan breaking changes immediately.

```kotlin
@ApiVersion("v1")  // Start with v1
@RestController
class UserController : SuperController<...>()
```

### 3. Maintain Old Versions

Don't immediately remove old versions. Give users time to migrate:

```kotlin
// Keep v1 running
@ApiVersion("v1", deprecated = true)
@RestController
class UserControllerV1 : SuperController<...>()

// Introduce v2
@ApiVersion("v2")
@RestController
class UserControllerV2 : SuperController<...>()
```

### 4. Use Semantic Versioning

Use `v1`, `v2`, `v3`, etc. for major versions:

```kotlin
@ApiVersion("v1")  // Initial version
@ApiVersion("v2")  // Breaking changes
@ApiVersion("v3")  // More breaking changes
```

### 5. Document Version Differences

```kotlin
/**
 * User API v2
 *
 * Changes from v1:
 * - Added bulk import endpoint
 * - Changed date format to ISO 8601
 * - Removed legacy fields: oldField1, oldField2
 */
@ApiVersion("v2")
@RestController
class UserControllerV2 : SuperController<...>()
```

### 6. Test All Versions

```kotlin
// Test that both versions work
class UserVersioningIntegrationTest {
    @Test
    fun `v1 should return users in old format`() { ... }

    @Test
    fun `v2 should return users in new format`() { ... }
}
```

### 7. Use Default Version

Always configure a default version for clients that don't specify one:

```properties
super-controller.versioning.default-version=v1
```

### 8. Add Deprecation Warnings

When deprecating a version, enable deprecation headers:

```properties
super-controller.versioning.add-deprecation-headers=true
```

```kotlin
@ApiVersion("v1", deprecated = true)
@RestController
class UserControllerV1 : SuperController<...>()
```

## How It Works

### Request Flow

1. **Request Arrives**: Client sends HTTP request
2. **Version Extraction**: `ApiVersionRequestCondition` extracts version based on strategy
3. **Route Matching**: Spring finds controller with matching `@ApiVersion`
4. **Handler Execution**: Matched controller handles the request
5. **Response**: Interceptor adds `X-API-Version` header to response

### URL Generation

The `ControllerUtil.baseUrl` property automatically adapts based on versioning strategy:

- **URI Strategy**: `baseUrl` includes version → `/v1/api/users`
- **Other Strategies**: `baseUrl` excludes version → `/api/users`

This ensures generated URLs (like in HATEOAS links) are correct for each strategy.

### Auto-Configuration

Versioning is auto-configured when enabled:

```kotlin
@Configuration
@ConditionalOnProperty("super-controller.versioning.enabled", havingValue = "true")
class VersioningAutoConfiguration {
    // Registers VersioningConfig
    // Registers ApiVersionInterceptor
    // Registers ApiVersionRequestMappingHandlerMapping (for non-URI strategies)
}
```

## Troubleshooting

### Controllers Not Routing

**Problem**: Requests return 404

**Solution**: Ensure `@ApiVersion` matches the version in your request and versioning is enabled

```properties
super-controller.versioning.enabled=true
```

### Ambiguous Mapping

**Problem**: Error "Ambiguous mapping" on startup

**Solution**: Ensure each version has unique controller classes

```kotlin
// ❌ Bad - same controller for both versions
@ApiVersion("v1")
@ApiVersion("v2")
class UserController : SuperController<...>()

// ✅ Good - separate controllers
@ApiVersion("v1")
class UserControllerV1 : SuperController<...>()

@ApiVersion("v2")
class UserControllerV2 : SuperController<...>()
```

### Version Not in URL

**Problem**: Expected `/v1/api/users` but got `/api/users`

**Solution**: Check that strategy is set to URI

```properties
super-controller.versioning.strategy=URI
```

### Header Not Working

**Problem**: Version header ignored

**Solution**: Verify header name matches configuration

```properties
super-controller.versioning.header-name=X-API-Version
```

```bash
# Must match header name
curl -H "X-API-Version: v1" http://localhost:8080/api/users
```

## Migration Guide

### From No Versioning to Versioning

1. **Enable versioning with default**:

```properties
super-controller.versioning.enabled=true
super-controller.versioning.strategy=HEADER
super-controller.versioning.default-version=v1
```

2. **Add @ApiVersion to existing controllers**:

```kotlin
@ApiVersion("v1")
@RestController
class UserController : SuperController<...>()
```

3. **Test thoroughly**: Ensure existing clients still work

4. **Update clients gradually**: Add version headers to client requests

### From v1 to v2

1. **Create v2 controller**:

```kotlin
@ApiVersion("v2")
@RestController
class UserControllerV2 : SuperController<...>()
```

2. **Mark v1 as deprecated**:

```kotlin
@ApiVersion("v1", deprecated = true)
@RestController
class UserControllerV1 : SuperController<...>()
```

3. **Enable deprecation headers**:

```properties
super-controller.versioning.add-deprecation-headers=true
```

4. **Notify clients**: Give migration timeline

5. **Remove v1**: After migration period, remove deprecated version

## See Also

- [ApiVersion Annotation](../src/main/kotlin/io/github/robertomike/super_controller/versioning/ApiVersion.kt)
- [VersioningConfig](../src/main/kotlin/io/github/robertomike/super_controller/versioning/VersioningConfig.kt)
- [ApiVersionRequestCondition](../src/main/kotlin/io/github/robertomike/super_controller/versioning/ApiVersionRequestCondition.kt)
- [Integration Tests](../mvc/src/test/kotlin/io/github/robertomike/super_controller/integration/VersioningStrategiesIntegrationTest.kt)
