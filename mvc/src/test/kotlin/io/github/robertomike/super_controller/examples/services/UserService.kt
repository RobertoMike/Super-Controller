package io.github.robertomike.super_controller.examples.services

import io.github.robertomike.super_controller.examples.mappers.UserRequestMapper
import io.github.robertomike.super_controller.examples.models.User
import io.github.robertomike.super_controller.examples.repositories.UserRepository
import io.github.robertomike.super_controller.examples.requests.StoreUserRequest
import io.github.robertomike.super_controller.services.SuperService
import io.github.robertomike.super_controller.services.bulk.BulkOperations
import io.github.robertomike.super_controller.services.softdelete.SoftDeletableService
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service

@Service
open class UserService(
    override val repository: UserRepository,
    override val mapper: UserRequestMapper
) : 
    SuperService<User, Long, StoreUserRequest, StoreUserRequest>(),
    BulkOperations<User, Page<User>, Long, StoreUserRequest, StoreUserRequest>,
    SoftDeletableService<User, Page<User>, Long, StoreUserRequest, StoreUserRequest> {
    
    override fun afterShow(model: User) {
        model.name = model.name?.uppercase()
    }
}