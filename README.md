# Super-Controller

A comprehensive Kotlin library for Spring Boot that eliminates boilerplate code and accelerates API development with automatic CRUD operations, API versioning, bulk operations, and more.

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE.txt)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-purple.svg)](https://kotlinlang.org)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-green.svg)](https://spring.io/projects/spring-boot)

## ✨ Features

- 🚀 **Automatic CRUD Endpoints** - Get complete REST APIs with a single class
- 📚 **API Versioning** - 4 strategies: URI, Header, Parameter, Accept-Header
- 🗑️ **Soft Delete** - Mark records as deleted without losing data
- ⚡ **Bulk Operations** - Create, update, delete multiple records at once
- 📖 **OpenAPI Documentation** - Automatic Swagger/OpenAPI 3.0 generation
- 🔒 **Policy-Based Authorization** - Flexible, reusable authorization rules
- 📄 **Pagination & Sorting** - Built-in support for large datasets
- 🎯 **Lifecycle Hooks** - Customize behavior at every step
- 🔧 **Highly Configurable** - Fine-tune every aspect via properties

## 📦 Installation

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

## 🚀 Quick Start

### 1. Create Your Entity

```kotlin
@Entity
data class User(
    @Id @GeneratedValue
    val id: Long? = null,
    val name: String,
    val email: String
)
```

### 2. Create Request DTOs

```kotlin
data class StoreUserRequest(
    @NotBlank val name: String,
    @Email val email: String
)

data class UpdateUserRequest(
    val name: String?,
    val email: String?
)
```

### 3. Create Service

```kotlin
@Service
class UserService(
    repository: UserRepository
) : SuperService<User, Long>(repository)
```

### 4. Create Controller

```kotlin
@RestController
class UserController(
    userService: UserService,
    override val mapper: UserResponseMapper
) : SuperController<User, Long, StoreUserRequest, UpdateUserRequest>(userService)
```

### 5. Done! 🎉

You now have a complete REST API:

```bash
GET    /api/users              # List all users (paginated)
GET    /api/users/{id}         # Get user by ID
POST   /api/users              # Create user
PUT    /api/users/{id}         # Update user
DELETE /api/users/{id}         # Delete user
```

## 📘 Documentation

### Core Documentation

- **[Complete Documentation](DOCUMENTATION.md)** - Comprehensive guide to all features
- **[API Versioning Guide](VERSIONING.md)** - Detailed versioning documentation
- **[OpenAPI Documentation](OPENAPI_DOCUMENTATION.md)** - OpenAPI/Swagger integration

### Quick Links

- [Basic CRUD Operations](DOCUMENTATION.md#basic-crud-operations)
- [API Versioning](DOCUMENTATION.md#api-versioning)
- [Soft Delete](DOCUMENTATION.md#soft-delete)
- [Bulk Operations](DOCUMENTATION.md#bulk-operations)
- [Caching](DOCUMENTATION.md#caching)
- [Authorization](DOCUMENTATION.md#authorization)
- [Examples](DOCUMENTATION.md#examples)

## 🎯 Key Features Overview

### API Versioning

Support multiple API versions simultaneously:

```kotlin
@ApiVersion("v1")
@RestController
class UserControllerV1 : SuperController<...>()

@ApiVersion("v2")
@RestController
class UserControllerV2 : SuperController<...>()
```

**4 Strategies:**

- **URI**: `/api/V1/users`, `/api/V2/users`
- **Header**: `X-API-Version: V1`
- **Parameter**: `/api/users?version=V1`
- **Accept-Header**: `Accept: application/vnd.api.V1+json`

### Soft Delete

Keep deleted records for audit trails:

```kotlin
@RestController
class UserController : SuperController<...>,
    SoftDeletableMarker<User, Long>  // Enable soft delete
```

**New endpoints:**

- `DELETE /users/{id}/soft-delete` - Soft delete
- `PUT /users/{id}/restore` - Restore
- `DELETE /users/{id}/force` - Permanent delete

### Bulk Operations

Handle multiple records efficiently:

```kotlin
@RestController
class UserController : SuperController<...>,
    BulkOperationsMarker<...>  // Enable bulk operations
```

**New endpoints:**

- `POST /users/bulk` - Create multiple
- `PUT /users/bulk` - Update multiple
- `DELETE /users/bulk` - Delete multiple

### OpenAPI Documentation

Automatic Swagger UI generation:

```properties
super-controller.openapi.enabled=true
super-controller.openapi.title=My API
```

Access at: `http://localhost:8080/swagger-ui.html`

## 🔧 Configuration

### application.properties

```properties
# Versioning
super-controller.versioning.enabled=true
super-controller.versioning.strategy=URI
super-controller.versioning.default-version=v1

# OpenAPI
super-controller.openapi.enabled=true
super-controller.openapi.title=My API

# General
super-controller.prefix-url=/api
```

See [Configuration Guide](DOCUMENTATION.md#configuration) for all options.

## 💡 Examples

### Complete User Management API

```kotlin
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
        return request.copy(email = request.email.lowercase())
    }

    override fun afterCreate(entity: User, request: StoreUserRequest): User {
        emailService.sendWelcomeEmail(entity)
        return entity
    }

    @GetMapping("/users/active")
    fun getActiveUsers(pageable: Pageable): Page<User> {
        return service.findActiveUsers(pageable)
    }
}
```

**Automatic endpoints:**

```
# CRUD
GET    /api/V1/users
POST   /api/V1/users
GET    /api/V1/users/{id}
PUT    /api/V1/users/{id}
DELETE /api/V1/users/{id}

# Bulk
POST   /api/V1/users/bulk
PUT    /api/V1/users/bulk
DELETE /api/V1/users/bulk

# Soft Delete
DELETE /api/V1/users/{id}/soft-delete
PUT    /api/V1/users/{id}/restore
DELETE /api/V1/users/{id}/force

# Custom
GET    /api/V1/users/active
```

More examples in [DOCUMENTATION.md](DOCUMENTATION.md#examples)

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📄 License

This project is licensed under the Apache License 2.0 - see the [LICENSE.txt](LICENSE.txt) file for details.

## 🔗 Links

- **GitHub**: [https://github.com/RobertoMike/Super-Controller](https://github.com/RobertoMike/Super-Controller)
- **Issues**: [https://github.com/RobertoMike/Super-Controller/issues](https://github.com/RobertoMike/Super-Controller/issues)

---

Made with ❤️ by [RobertoMike](https://github.com/RobertoMike)
