package io.github.robertomike.super_controller.reactive.examples.mappers

import io.github.robertomike.super_controller.reactive.examples.models.User
import io.github.robertomike.super_controller.reactive.examples.requests.StoreUserRequest
import io.github.robertomike.super_controller.reactive.examples.requests.UpdateUserRequest
import io.github.robertomike.super_controller.mappers.RequestMapper
import org.mapstruct.Mapper

@Mapper
interface UserRequestMapper : RequestMapper<User, StoreUserRequest, UpdateUserRequest>