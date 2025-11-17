# Super-Controller Documentation

A comprehensive Kotlin library for Spring Boot that simplifies CRUD operations, API versioning, caching, bulk operations, and more.

## Table of Contents

- [Overview](#overview)
- [Installation](#installation)
- [Core Features](#core-features)
  - [Basic CRUD Operations](#basic-crud-operations)
  - [SuperController](#supercontroller)
  - [SuperService](#superservice)
- [Advanced Features](#advanced-features)
  - [API Versioning](#api-versioning)
  - [Soft Delete](#soft-delete)
  - [Bulk Operations](#bulk-operations)
  - [Caching](#caching)
  - [OpenAPI Documentation](#openapi-documentation)
  - [Authorization](#authorization)
- [Configuration](#configuration)
- [Examples](#examples)
- [Best Practices](#best-practices)

---

## Overview

Super-Controller is a Kotlin library that provides base classes and utilities to quickly build RESTful APIs with Spring Boot. It eliminates boilerplate code and provides:

- ✅ Automatic CRUD endpoints
- ✅ Built-in pagination and sorting
- ✅ API versioning (URI, Header, Parameter, Accept-Header)
- ✅ Soft delete support
- ✅ Bulk operations (create, update, delete)
- ✅ Automatic OpenAPI documentation
- ✅ Caching with custom strategies
- ✅ Policy-based authorization
- ✅ Request/Response mapping

---

## Installation

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("io.github.robertomike:super-controller:$version")
}
```

### Maven

```xml
<dependency>
    <groupId>io.github.robertomike</groupId>
    <artifactId>super-controller</artifactId>
    <version>${version}</version>
</dependency>
```

---

## Core Features

### Basic CRUD Operations

Super-Controller automatically provides these endpoints when you extend `SuperController`:

| Method | Endpoint         | Description                    |
| ------ | ---------------- | ------------------------------ |
| GET    | `/resource`      | List all resources (paginated) |
| GET    | `/resource/{id}` | Get a single resource by ID    |
| POST   | `/resource`      | Create a new resource          |
| PUT    | `/resource/{id}` | Update an existing resource    |
| DELETE | `/resource/{id}` | Delete a resource              |

### SuperController

The core class that provides CRUD endpoints.

#### Basic Usage

```kotlin
@RestController
class UserController(
    userService: UserService,
    override val mapper: UserResponseMapper
) : SuperController<User, Long, StoreUserRequest, UpdateUserRequest>(userService)
```

**Type Parameters:**

- `M`: Model/Entity class (e.g., `User`)
- `I`: ID type (e.g., `Long`, `UUID`)
- `Req`: Request class for creating
- `UpReq`: Request class for updating

#### Customization

```kotlin
@RestController
class UserController(
    userService: UserService,
    override val mapper: UserResponseMapper
) : SuperController<User, Long, StoreUserRequest, UpdateUserRequest>(userService) {

    init {
        // Customize behavior
        needAuthorization = true
        policy = UserPolicy::class.java
        path = "users"  // Custom path (default: lowercase class name)
    }

    // Override lifecycle hooks
    override fun beforeCreate(request: StoreUserRequest): StoreUserRequest {
        // Modify request before creation
        return request.copy(email = request.email.lowercase())
    }

    override fun afterCreate(entity: User, request: StoreUserRequest): User {
        // Do something after creation (e.g., send email)
        emailService.sendWelcomeEmail(entity)
        return entity
    }

    // Custom endpoints
    @GetMapping("/users/active")
    fun getActiveUsers(): List<User> {
        return service.findByActive(true)
    }
}
```

#### Lifecycle Hooks

Override these methods to customize behavior:

**Before hooks** (modify request/entity before operation):

- `beforeCreate(request: Req): Req`
- `beforeUpdate(id: I, request: UpReq): UpReq`
- `beforeDestroy(id: I)`

**After hooks** (execute logic after operation):

- `afterCreate(entity: M, request: Req): M`
- `afterUpdate(entity: M, request: UpReq): M`
- `afterDestroy(id: I)`

**Query hooks** (customize queries):

- `beforeIndex(filters: Map<String, Any>): Map<String, Any>`
- `beforeShow(id: I)`

### SuperService

The service layer that handles business logic and database operations.

#### Basic Usage

```kotlin
@Service
class UserService(
    repository: UserRepository
) : SuperService<User, Long>(repository)
```

#### Custom Queries

```kotlin
@Service
class UserService(
    repository: UserRepository
) : SuperService<User, Long>(repository) {

    // Add custom methods
    fun findByEmail(email: String): User? {
        return repository.findByEmail(email)
    }

    fun findActiveUsers(): List<User> {
        return repository.findByActiveTrue()
    }

    // Override to add custom filters
    override fun applyFilters(
        filters: Map<String, Any>,
        page: Pageable
    ): Page<User> {
        // Custom filtering logic
        val email = filters["email"] as? String

        return if (email != null) {
            repository.findByEmailContaining(email, page)
        } else {
            super.applyFilters(filters, page)
        }
    }
}
```

---

## Advanced Features

### API Versioning

Super-Controller supports 4 versioning strategies:

#### 1. URI Strategy

Version in URL path.

**Configuration:**

```properties
super-controller.versioning.enabled=true
super-controller.versioning.strategy=URI
super-controller.versioning.default-version=v1
```

**Controllers:**

```kotlin
@ApiVersion("v1")
@RestController
class UserControllerV1(
    userService: UserService,
    override val mapper: UserResponseMapper
) : SuperController<User, Long, StoreUserRequest, UpdateUserRequest>(userService)

@ApiVersion("v2")
@RestController
class UserControllerV2(
    userServiceV2: UserServiceV2,
    override val mapper: UserResponseMapperV2
) : SuperController<User, Long, StoreUserRequestV2, UpdateUserRequestV2>(userServiceV2)
```

**Requests:**

```bash
GET /api/V1/users  # Routes to UserControllerV1
GET /api/V2/users  # Routes to UserControllerV2
```

#### 2. Header Strategy

Version in HTTP header.

**Configuration:**

```properties
super-controller.versioning.strategy=HEADER
super-controller.versioning.header-name=X-API-Version
```

**Request:**

```bash
GET /api/users
X-API-Version: v1
```

#### 3. Parameter Strategy

Version in query parameter.

**Configuration:**

```properties
super-controller.versioning.strategy=PARAMETER
super-controller.versioning.param-name=version
```

**Request:**

```bash
GET /api/users?version=v1
```

#### 4. Accept Header Strategy

Version in Accept header.

**Configuration:**

```properties
super-controller.versioning.strategy=ACCEPT_HEADER
super-controller.versioning.media-type-prefix=application/vnd.api
```

**Request:**

```bash
GET /api/users
Accept: application/vnd.api.v1+json
```

#### Deprecation

Mark old versions as deprecated:

```kotlin
@ApiVersion("v1", deprecated = true, sunset = "2024-12-31")
@RestController
class UserControllerV1 : SuperController<...>()
```

**Configuration:**

```properties
super-controller.versioning.add-deprecation-headers=true
```

**Response headers:**

```
X-API-Version: v1
X-API-Deprecated: true
X-API-Sunset: 2024-12-31
```

📖 **See [VERSIONING.md](VERSIONING.md) for detailed versioning documentation.**

---

### Soft Delete

Soft delete allows marking records as deleted without physically removing them from the database.

#### Enable Soft Delete

**1. Entity with `deletedAt` field:**

```kotlin
@Entity
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val name: String,
    val email: String,

    @Column(name = "deleted_at")
    var deletedAt: LocalDateTime? = null
)
```

**2. Controller implements SoftDeletableMarker:**

```kotlin
@RestController
class UserController(
    userService: UserService,
    override val mapper: UserResponseMapper
) : SuperController<User, Long, StoreUserRequest, UpdateUserRequest>(userService),
    SoftDeletableMarker<User, Long>  // Add this marker
```

#### New Endpoints

| Method | Endpoint                  | Description                  |
| ------ | ------------------------- | ---------------------------- |
| DELETE | `/users/{id}/soft-delete` | Soft delete (sets deletedAt) |
| PUT    | `/users/{id}/restore`     | Restore soft-deleted record  |
| DELETE | `/users/{id}/force`       | Permanently delete           |

#### Usage

```bash
# Soft delete
DELETE /api/users/1/soft-delete
# Sets user.deletedAt = now()

# Restore
PUT /api/users/1/restore
# Sets user.deletedAt = null

# Force delete (permanent)
DELETE /api/users/1/force
# Physically removes from database
```

---

### Bulk Operations

Perform operations on multiple records at once.

#### Enable Bulk Operations

**Controller implements BulkOperationsMarker:**

```kotlin
@RestController
class UserController(
    userService: UserService,
    override val mapper: UserResponseMapper
) : SuperController<User, Long, StoreUserRequest, UpdateUserRequest>(userService),
    BulkOperationsMarker<User, Long, StoreUserRequest, UpdateUserRequest>  // Add this marker
```

#### New Endpoints

| Method | Endpoint      | Description             |
| ------ | ------------- | ----------------------- |
| POST   | `/users/bulk` | Create multiple records |
| PUT    | `/users/bulk` | Update multiple records |
| DELETE | `/users/bulk` | Delete multiple records |

#### Usage

**Bulk Create:**

```bash
POST /api/users/bulk
Content-Type: application/json

[
  {
    "name": "User 1",
    "email": "user1@example.com"
  },
  {
    "name": "User 2",
    "email": "user2@example.com"
  }
]
```

**Bulk Update:**

```bash
PUT /api/users/bulk
Content-Type: application/json

{
  "1": {
    "name": "Updated User 1",
    "email": "updated1@example.com"
  },
  "2": {
    "name": "Updated User 2",
    "email": "updated2@example.com"
  }
}
```

**Bulk Delete:**

```bash
DELETE /api/users/bulk
Content-Type: application/json

[1, 2, 3, 4, 5]
```

---

### Caching

SuperController provides **automatic caching** for service layer operations through the `@SuperCache` annotation and AOP (Aspect-Oriented Programming).

#### How It Works

SuperController uses AspectJ to intercept service method calls and automatically cache results. The caching is applied at the **service layer**, not the controller layer, making it work seamlessly with the programmatically registered CRUD endpoints.

**Key Components:**

1. **`@SuperCache` Annotation**: Applied to service classes to enable automatic caching
2. **`CacheAspect`**: AOP aspect that intercepts service methods and manages cache operations
3. **Cache Keys**: Automatically generated based on entity IDs and method parameters

#### Cache Key Format

Cache keys follow the pattern: `{prefix}:{key}`

- **prefix**: Either the custom prefix from `@SuperCache` or the service class name
- **key**: The entity ID or a generated key from method parameters

**Examples:**

- Single entity: `UserService:123` (user with ID 123)
- Index/list: `UserService:page=0_size=10_sort=name` (paginated list with parameters)

#### Enable Caching

**1. Add Spring Boot Cache dependency:**

```gradle
implementation("org.springframework.boot:spring-boot-starter-cache")
```

**2. Enable caching in your application:**

```kotlin
@SpringBootApplication
@EnableCaching
class Application
```

**3. Configure cache provider (optional):**

```properties
# Simple in-memory cache (default)
spring.cache.type=simple

# Or use Redis, Caffeine, etc.
spring.cache.type=redis
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

#### Usage

**Apply `@SuperCache` to your service:**

```kotlin
@Service
@SuperCache(
    value = "users",              // Cache name (required)
    prefix = "user",              // Cache key prefix (optional, defaults to class name)
    saveIndex = true,             // Cache index/list results (default: true)
    saveSingle = true,            // Cache single entity results (default: true)
    saveOnStore = false           // Cache newly created entities (default: false)
)
class UserService(
    override val repository: UserRepository
) : SuperService<User, Long>(repository)
```

**That's it!** The caching is now automatic for all CRUD operations:

- **`index()`** → Cached with parameter-based key (if `saveIndex = true`)
- **`findById()`** → Cached with entity ID (if `saveSingle = true`)
- **`store()`** → Optionally cached (if `saveOnStore = true`)
- **`update()`** → Updates cache automatically
- **`delete()`** → Evicts from cache automatically

#### Configuration Options

```kotlin
@SuperCache(
    value = "users",          // Required: Cache name for Spring CacheManager
    prefix = "user",          // Optional: Custom prefix for cache keys (defaults to class name)
    saveIndex = true,         // Cache paginated list results (default: true)
    saveSingle = true,        // Cache single entity lookups (default: true)
    saveOnStore = false       // Cache entities on creation (default: false, to avoid stale data)
)
```

**When to use `saveOnStore = true`:**

- Read-heavy applications where new entities are frequently queried immediately after creation
- When you control the entire entity lifecycle

**When to keep `saveOnStore = false` (default):**

- Write-heavy applications
- When external systems might modify entities after creation
- To prevent stale cached data

#### Automatic Cache Invalidation

The cache is automatically managed for all operations:

| Operation               | Cache Behavior                                           |
| ----------------------- | -------------------------------------------------------- |
| **Create** (`store()`)  | Optionally caches the new entity if `saveOnStore = true` |
| **Read** (`findById()`) | Caches the result if `saveSingle = true`                 |
| **Update** (`update()`) | Updates the cached entity                                |
| **Delete** (`delete()`) | Evicts the entity from cache                             |
| **Index** (`index()`)   | Caches paginated results if `saveIndex = true`           |

#### Bulk Operations Support

Caching also works with bulk operations:

```kotlin
@Service
@SuperCache(value = "users", prefix = "user")
class UserService(
    override val repository: UserRepository
) : SuperService<User, Long>(repository), BulkOperations<User, Long, StoreUserRequest, UpdateUserRequest>
```

**Bulk caching behavior:**

- **`bulkStore()`** → Caches successful entities if `saveOnStore = true`
- **`bulkUpdate()`** → Updates cache for all successful entities
- **`bulkDelete()`** → Evicts all successfully deleted entities from cache

#### Soft Delete Support

Caching works seamlessly with soft delete:

```kotlin
@Service
@SuperCache(value = "users", prefix = "user")
class UserService(
    override val repository: UserRepository
) : SuperService<User, Long>(repository), SoftDeletableService<User, Long>
```

**Soft delete caching behavior:**

- **`softDelete()`** → Evicts entity from cache
- **`restore()`** → Caches the restored entity if `saveSingle = true`
- **`forceDelete()`** → Evicts entity from cache permanently

#### Custom Endpoints with Caching

For custom endpoints, you can still use Spring's standard caching annotations:

```kotlin
@RestController
class UserController(
    userService: UserService,
    override val mapper: UserResponseMapper
) : SuperController<User, Long, StoreUserRequest, UpdateUserRequest>(userService) {

    // Cache active users list
    @Cacheable("users:active")
    @GetMapping("/users/active")
    fun getActiveUsers(): List<User> {
        return service.findByActive(true)
    }

    // Evict cache when activating a user
    @CacheEvict(value = ["users:active"], allEntries = true)
    @PutMapping("/users/activate/{id}")
    fun activateUser(@PathVariable id: Long): User {
        return service.activate(id)
    }

    // Cache with custom key
    @Cacheable(value = ["users:stats"], key = "#userId")
    @GetMapping("/users/{userId}/stats")
    fun getUserStats(@PathVariable userId: Long): UserStats {
        return service.calculateStats(userId)
    }
}
```

#### Advanced: Cache Manager Configuration

Configure cache behavior globally:

```kotlin
@Configuration
@EnableCaching
class CacheConfig {

    @Bean
    fun cacheManager(): CacheManager {
        return CaffeineCacheManager().apply {
            setCaffeine(
                Caffeine.newBuilder()
                    .expireAfterWrite(1, TimeUnit.HOURS)
                    .maximumSize(1000)
            )
        }
    }
}
```

**Or with Redis:**

```kotlin
@Configuration
@EnableCaching
class CacheConfig {

    @Bean
    fun cacheManager(connectionFactory: RedisConnectionFactory): CacheManager {
        val config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(1))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    GenericJackson2JsonRedisSerializer()
                )
            )

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .build()
    }
}
```

#### Example: Complete Cached Service

```kotlin
@Service
@SuperCache(
    value = "products",
    prefix = "product",
    saveIndex = true,
    saveSingle = true,
    saveOnStore = false
)
class ProductService(
    override val repository: ProductRepository
) : SuperService<Product, Long>(repository),
    BulkOperations<Product, Long, StoreProductRequest, UpdateProductRequest>,
    SoftDeletableService<Product, Long> {

    // All CRUD operations are automatically cached:
    // - index() → Cached with pagination params
    // - findById() → Cached with product ID
    // - store() → Not cached (saveOnStore = false)
    // - update() → Updates cache
    // - delete() → Evicts from cache
    // - bulkStore/Update/Delete() → Cached accordingly
    // - softDelete/restore/forceDelete() → Cache managed automatically
}
```

**Cache keys generated:**

- `product:123` → Product with ID 123
- `product:page=0_size=10` → First page of products
- `product:page=0_size=10_category=electronics` → Filtered results

````

---

### OpenAPI Documentation

Automatic OpenAPI 3.0 documentation generation.

#### Enable OpenAPI

**Configuration:**

```properties
super-controller.openapi.enabled=true
super-controller.openapi.title=My API
super-controller.openapi.description=API Documentation
super-controller.openapi.version=1.0.0
super-controller.openapi.servers[0].url=https://api.example.com
super-controller.openapi.servers[0].description=Production
````

#### Features

- **Automatic endpoint documentation** for CRUD operations
- **Bulk operations documentation** when enabled
- **Soft delete endpoints documentation** when enabled
- **Pagination parameters** on list endpoints
- **Request/Response schemas** from your DTOs
- **Tags** for grouping endpoints
- **Deprecation warnings** for old API versions

#### Access Documentation

```bash
# Swagger UI
http://localhost:8080/swagger-ui.html

# OpenAPI JSON
http://localhost:8080/v3/api-docs
```

#### Customization

```kotlin
@RestController
@Tag(name = "Users", description = "User management endpoints")
class UserController(
    userService: UserService,
    override val mapper: UserResponseMapper
) : SuperController<User, Long, StoreUserRequest, UpdateUserRequest>(userService) {

    @Operation(
        summary = "Get active users",
        description = "Retrieve all active users in the system"
    )
    @GetMapping("/users/active")
    fun getActiveUsers(): List<User> {
        return service.findByActive(true)
    }
}
```

#### Advanced Configuration

```properties
# Full OpenAPI configuration
super-controller.openapi.enabled=true
super-controller.openapi.title=My API
super-controller.openapi.description=Comprehensive API for my application
super-controller.openapi.version=1.0.0
super-controller.openapi.contact.name=API Support
super-controller.openapi.contact.email=support@example.com
super-controller.openapi.license.name=Apache 2.0
super-controller.openapi.license.url=https://www.apache.org/licenses/LICENSE-2.0
super-controller.openapi.servers[0].url=https://api.example.com
super-controller.openapi.servers[0].description=Production Server
super-controller.openapi.servers[1].url=https://staging.api.example.com
super-controller.openapi.servers[1].description=Staging Server
```

#### Integration with Tools

**Postman Import:**

1. Export OpenAPI JSON from `/v3/api-docs`
2. Import into Postman as API definition
3. Auto-generate requests for all endpoints

**Client SDK Generation:**

```bash
# Generate TypeScript client
openapi-generator-cli generate \
  -i http://localhost:8080/v3/api-docs \
  -g typescript-axios \
  -o ./generated/typescript-client
```

---

---

### Authorization

Policy-based authorization for CRUD operations.

#### Create Policy

```kotlin
@Component
class UserPolicy : Policy<User, Long> {

    override fun canIndex(principal: Any?): Boolean {
        // Everyone can list users
        return true
    }

    override fun canShow(entity: User, principal: Any?): Boolean {
        // Everyone can view users
        return true
    }

    override fun canStore(principal: Any?): Boolean {
        // Only admins can create users
        return principal is Admin
    }

    override fun canUpdate(entity: User, principal: Any?): Boolean {
        // Users can update themselves, admins can update anyone
        return when (principal) {
            is Admin -> true
            is User -> principal.id == entity.id
            else -> false
        }
    }

    override fun canDestroy(entity: User, principal: Any?): Boolean {
        // Only admins can delete users
        return principal is Admin
    }
}
```

#### Apply Policy

```kotlin
@RestController
class UserController(
    userService: UserService,
    override val mapper: UserResponseMapper
) : SuperController<User, Long, StoreUserRequest, UpdateUserRequest>(userService) {

    init {
        needAuthorization = true
        policy = UserPolicy::class.java
    }
}
```

#### Selective Authorization

```kotlin
init {
    needAuthorization = true
    policy = UserPolicy::class.java

    // Only apply authorization to specific endpoints
    onlyUrls = listOf(Methods.STORE, Methods.UPDATE, Methods.DESTROY)

    // Or exclude specific endpoints
    exceptUrls = listOf(Methods.INDEX, Methods.SHOW)
}
```

---

## Configuration

### Application Properties

```properties
# Versioning
super-controller.versioning.enabled=true
super-controller.versioning.strategy=URI
super-controller.versioning.default-version=v1
super-controller.versioning.header-name=X-API-Version
super-controller.versioning.param-name=version
super-controller.versioning.media-type-prefix=application/vnd.api
super-controller.versioning.add-deprecation-headers=true

# Caching
super-controller.cache.enabled=true
super-controller.cache.strategy=SIMPLE
super-controller.cache.ttl=3600

# OpenAPI
super-controller.openapi.enabled=true
super-controller.openapi.title=My API
super-controller.openapi.description=API Documentation
super-controller.openapi.version=1.0.0
super-controller.openapi.contact.name=Support Team
super-controller.openapi.contact.email=support@example.com
super-controller.openapi.contact.url=https://example.com/support
super-controller.openapi.license.name=Apache 2.0
super-controller.openapi.license.url=https://www.apache.org/licenses/LICENSE-2.0
super-controller.openapi.servers[0].url=https://api.example.com
super-controller.openapi.servers[0].description=Production

# General
super-controller.prefix-url=/api
super-controller.base-package=com.example.yourapp
```

---

## Examples

### Complete User Management API

```kotlin
// Entity
@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false, unique = true)
    var email: String,

    var active: Boolean = true,

    @Column(name = "deleted_at")
    var deletedAt: LocalDateTime? = null,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
)

// Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): User?
    fun findByActiveTrue(pageable: Pageable): Page<User>
}

// Requests
data class StoreUserRequest(
    @NotBlank
    val name: String,

    @Email
    @NotBlank
    val email: String
)

data class UpdateUserRequest(
    val name: String?,
    val email: String?,
    val active: Boolean?
)

// Response Mapper
@Component
class UserResponseMapper : ResponseMapper<User> {
    override fun map(entity: User): Any {
        return mapOf(
            "id" to entity.id,
            "name" to entity.name,
            "email" to entity.email,
            "active" to entity.active,
            "createdAt" to entity.createdAt,
            "updatedAt" to entity.updatedAt
        )
    }
}

// Service
@Service
class UserService(
    repository: UserRepository
) : SuperService<User, Long>(repository) {

    fun findByEmail(email: String): User? {
        return repository.findByEmail(email)
    }

    fun findActiveUsers(pageable: Pageable): Page<User> {
        return repository.findByActiveTrue(pageable)
    }
}

// Controller
@ApiVersion("v1")
@RestController
class UserController(
    userService: UserService,
    override val mapper: UserResponseMapper
) : SuperController<User, Long, StoreUserRequest, UpdateUserRequest>(userService),
    BulkOperationsMarker<User, Long, StoreUserRequest, UpdateUserRequest>,
    SoftDeletableMarker<User, Long> {

    init {
        needAuthorization = true
        policy = UserPolicy::class.java
    }

    override fun beforeCreate(request: StoreUserRequest): StoreUserRequest {
        // Normalize email
        return request.copy(email = request.email.lowercase())
    }

    override fun afterCreate(entity: User, request: StoreUserRequest): User {
        // Send welcome email
        emailService.sendWelcomeEmail(entity)
        return entity
    }

    @GetMapping("/users/active")
    fun getActiveUsers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): Page<Any> {
        val pageable = PageRequest.of(page, size)
        return service.findActiveUsers(pageable).map { mapper.map(it) }
    }
}
```

### Available Endpoints

```
# CRUD
GET    /api/V1/users              # List users (paginated)
GET    /api/V1/users/{id}         # Get user
POST   /api/V1/users              # Create user
PUT    /api/V1/users/{id}         # Update user
DELETE /api/V1/users/{id}         # Delete user

# Bulk Operations
POST   /api/V1/users/bulk         # Create multiple users
PUT    /api/V1/users/bulk         # Update multiple users
DELETE /api/V1/users/bulk         # Delete multiple users

# Soft Delete
DELETE /api/V1/users/{id}/soft-delete  # Soft delete
PUT    /api/V1/users/{id}/restore      # Restore
DELETE /api/V1/users/{id}/force        # Force delete

# Custom
GET    /api/V1/users/active       # Get active users
```

---

## Best Practices

### 1. Use DTOs for Requests and Responses

```kotlin
// ❌ Bad - exposing entity directly
class UserController : SuperController<User, Long, User, User>

// ✅ Good - using DTOs
class UserController : SuperController<User, Long, StoreUserRequest, UpdateUserRequest>
```

### 2. Implement ResponseMapper

```kotlin
// ❌ Bad - no mapper, returns entities as-is
override val mapper: ResponseMapper<User>? = null

// ✅ Good - custom response mapping
@Component
class UserResponseMapper : ResponseMapper<User> {
    override fun map(entity: User): Any {
        return UserResponse(
            id = entity.id!!,
            name = entity.name,
            email = entity.email
        )
    }
}
```

### 3. Version Your APIs Early

```kotlin
// ✅ Start with v1 from the beginning
@ApiVersion("v1")
@RestController
class UserController : SuperController<...>()
```

### 4. Use Lifecycle Hooks for Business Logic

```kotlin
// ✅ Keep business logic in hooks
override fun beforeCreate(request: StoreUserRequest): StoreUserRequest {
    // Validation, normalization
    return request.copy(email = request.email.lowercase())
}

override fun afterCreate(entity: User, request: StoreUserRequest): User {
    // Side effects
    emailService.sendWelcomeEmail(entity)
    auditService.log("User created: ${entity.id}")
    return entity
}
```

### 5. Implement Proper Authorization

```kotlin
// ✅ Use policy-based authorization
init {
    needAuthorization = true
    policy = UserPolicy::class.java
    onlyUrls = listOf(Methods.STORE, Methods.UPDATE, Methods.DESTROY)
}
```

### 6. Add Pagination Parameters

```kotlin
// ✅ Support pagination in custom endpoints
@GetMapping("/users/search")
fun search(
    @RequestParam query: String,
    @RequestParam(defaultValue = "0") page: Int,
    @RequestParam(defaultValue = "10") size: Int
): Page<Any> {
    val pageable = PageRequest.of(page, size)
    return service.search(query, pageable).map { mapper.map(it) }
}
```

### 7. Document Custom Endpoints

```kotlin
// ✅ Add OpenAPI annotations
@Operation(
    summary = "Search users",
    description = "Search users by name or email"
)
@ApiResponses(
    value = [
        ApiResponse(responseCode = "200", description = "Success"),
        ApiResponse(responseCode = "400", description = "Invalid query")
    ]
)
@GetMapping("/users/search")
fun search(@Parameter(description = "Search query") @RequestParam query: String): List<User> {
    return service.search(query)
}
```

### 8. Handle Errors Gracefully

```kotlin
// ✅ Throw appropriate exceptions
override fun beforeShow(id: Long) {
    if (id < 0) {
        throw IllegalArgumentException("Invalid user ID")
    }
}
```

### 9. Use Soft Delete for Audit Trails

```kotlin
// ✅ Implement soft delete for important entities
@RestController
class UserController : SuperController<User, Long, StoreUserRequest, UpdateUserRequest>,
    SoftDeletableMarker<User, Long>
```

### 10. Test Your APIs

```kotlin
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `should list users`() {
        mockMvc.perform(get("/api/V1/users"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
    }

    @Test
    fun `should create user`() {
        val request = """
            {
                "name": "Test User",
                "email": "test@example.com"
            }
        """

        mockMvc.perform(
            post("/api/V1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").exists())
    }
}
```

---

## Support

For issues, questions, or contributions, please visit:

- **GitHub**: [https://github.com/RobertoMike/Super-Controller](https://github.com/RobertoMike/Super-Controller)
- **Documentation**: [https://github.com/RobertoMike/Super-Controller/wiki](https://github.com/RobertoMike/Super-Controller/wiki)

## License

This project is licensed under the Apache License 2.0. See [LICENSE.txt](LICENSE.txt) for details.
