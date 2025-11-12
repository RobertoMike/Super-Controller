# Service Marker Interfaces

This directory contains marker interfaces that extend service functionality.

## Directory Structure

```
services/
├── bulk/
│   ├── BulkOperations.kt       - Service interface for bulk operations
│   └── BulkResults.kt          - Result classes for bulk operations
├── softdelete/
│   └── SoftDeletableService.kt - Service interface for soft delete operations
└── interfaces/
    └── BasicService.kt         - Base service interface
```

## Available Markers

### BulkOperations
**Location:** `bulk/BulkOperations.kt`

Service-level interface for bulk create, update, and delete operations.

**Generic Parameters:**
- `M` - Model type
- `ID` - ID type
- `SR` - Store Request type
- `UR` - Update Request type

**Methods:**
- `bulkStore(requests: List<SR>): BulkResult<M>`
- `bulkUpdate(updates: Map<ID, UR>): BulkResult<M>`
- `bulkDelete(ids: List<ID>): BulkDeleteResult`

**Example:**
```kotlin
class UserService : SuperService<User, Long, StoreUserRequest, UpdateUserRequest>(),
    BulkOperations<User, Long, StoreUserRequest, UpdateUserRequest> {
    
    override fun bulkStore(requests: List<StoreUserRequest>): BulkResult<User> {
        // Implementation
    }
}
```

---

### SoftDeletableService
**Location:** `softdelete/SoftDeletableService.kt`

Service-level interface for soft delete operations.

**Generic Parameters:**
- `M` - Model type (must implement `SoftDeletableEntity`)
- `ID` - ID type

**Methods:**
- `softDelete(id: ID): M` - Soft delete entity
- `restore(id: ID): M` - Restore soft deleted entity
- `forceDelete(id: ID)` - Permanently delete entity
- `isSoftDeleted(id: ID): Boolean` - Check if entity is soft deleted

**Example:**
```kotlin
class UserService(
    private val repository: UserRepository
) : SuperService<User, Long, UserRequest, UserRequest>(),
    SoftDeletableService<User, Long> {
    
    override fun softDelete(id: Long): User {
        val user = findById(id)
        user.markAsDeleted()
        return repository.save(user)
    }
    
    override fun restore(id: Long): User {
        val user = findById(id)
        if (!user.isDeleted()) {
            throw IllegalStateException("User is not soft deleted")
        }
        user.restore()
        return repository.save(user)
    }
    
    override fun forceDelete(id: Long) {
        val user = findById(id)
        repository.delete(user)
    }
    
    override fun isSoftDeleted(id: Long): Boolean {
        return findById(id).isDeleted()
    }
}
```

---

## Usage Notes

1. **Separation of Concerns:** Service markers define business logic, while controller markers (in `controllers/markers/`) define HTTP endpoints.

2. **Composition:** Services can implement multiple marker interfaces as needed.

3. **Type Safety:** All marker interfaces use proper generic types for compile-time type checking.

4. **Extensibility:** Services can override methods to provide custom implementations while maintaining the interface contract.

## Integration with Controllers

Controllers use their corresponding service markers through the controller marker interfaces:

```kotlin
// Service layer
class UserService : SuperService<...>(), 
    BulkOperations<...>,
    SoftDeletableService<...>

// Controller layer
class UserController(service: UserService) : SuperController<...>(),
    BulkOperations<...>,
    SoftDeletable<...> {
    
    override fun getBulkService() = service
    override fun getSoftDeleteService() = service
}
```

The controller delegates to the service for business logic while handling HTTP concerns.
