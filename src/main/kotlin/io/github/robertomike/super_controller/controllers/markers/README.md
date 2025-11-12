# Controller Marker Interfaces

This directory contains marker interfaces that extend SuperController functionality.

## Available Markers

### BulkOperations
**File:** `BulkOperations.kt`

Adds bulk create, update, and delete operations to your controller.

**Generic Parameters:**
- `M` - Model type
- `ID` - ID type
- `SR` - Store Request type (for creating entities)
- `UR` - Update Request type (for updating entities)

**Added Endpoints:**
- `POST /bulk` - Bulk create entities
- `PUT /bulk` - Bulk update entities
- `DELETE /bulk` - Bulk delete entities

**Example:**
```kotlin
@RestController
class UserController(service: UserService) : 
    SuperController<User, Long, StoreUserRequest, UpdateUserRequest>(service),
    BulkOperations<User, Long, StoreUserRequest, UpdateUserRequest>
```

**Hook Methods:**
- `beforeBulkStoreItem(request: SR)` / `afterBulkStoreItem(entity: M, request: SR)`
- `beforeBulkUpdateItem(entity: M, request: UR)` / `afterBulkUpdateItem(entity: M, request: UR)`
- `beforeBulkDeleteItem(entity: M)` / `afterBulkDeleteItem(id: ID)`

---

### SoftDeletable
**File:** `SoftDeletable.kt`

Adds soft delete, restore, and force delete operations to your controller.

**Generic Parameters:**
- `M` - Model type (must implement `SoftDeletableEntity`)
- `ID` - ID type

**Added Endpoints:**
- `POST /{id}/soft-delete` - Mark entity as deleted
- `POST /{id}/restore` - Restore soft deleted entity
- `DELETE /{id}/force` - Permanently delete entity

**Required Implementation:**
```kotlin
override fun saveEntity(entity: M): M = repository.save(entity)
```

**Example:**
```kotlin
@RestController
class UserController(
    service: UserService,
    private val userRepository: UserRepository
) : 
    SuperController<User, Long, UserRequest, UserRequest>(service),
    SoftDeletable<User, Long> {
    
    override fun saveEntity(entity: User): User = userRepository.save(entity)
}
```

**Hook Methods:**
- `beforeSoftDelete(entity: M)` / `afterSoftDelete(entity: M)`
- `beforeRestore(entity: M)` / `afterRestore(entity: M)`
- `beforeForceDelete(entity: M)` / `afterForceDelete(id: ID)`

---

## Usage Notes

1. **No Spring Annotations:** Interface methods are NOT annotated with Spring mappings to avoid duplicate routes. Routes are registered by `BaseRouter`.

2. **Type Safety:** Marker interfaces use proper generic types (SR, UR) instead of the base `Request` interface, providing compile-time type checking.

3. **Customization:** All marker interfaces provide hook methods that can be overridden to add custom logic before and after operations.

4. **Multiple Markers:** Controllers can implement multiple marker interfaces simultaneously:

```kotlin
@RestController
class UserController : 
    SuperController<User, Long, StoreUserRequest, UpdateUserRequest>(),
    BulkOperations<User, Long, StoreUserRequest, UpdateUserRequest>,
    SoftDeletable<User, Long> {
    // Implementation
}
```

## Architecture

- **Controller Markers** (this directory): Define HTTP endpoints and route handling
- **Service Markers** (`services/bulk/`, `services/softdelete/`): Define service-level operations
- **Model Interfaces** (`models/`): Define entity capabilities (e.g., `SoftDeletableEntity`)

See `Markers.kt` for convenient imports of all marker interfaces.
