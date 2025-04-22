package io.github.robertomike.super_controller.examples.mappers

import io.github.robertomike.super_controller.examples.models.User
import io.github.robertomike.super_controller.examples.requests.StoreUserRequest
import io.github.robertomike.super_controller.mappers.RequestMapper
import org.mapstruct.Mapper

@Mapper
interface UserRequestMapper : RequestMapper<User, StoreUserRequest, StoreUserRequest>