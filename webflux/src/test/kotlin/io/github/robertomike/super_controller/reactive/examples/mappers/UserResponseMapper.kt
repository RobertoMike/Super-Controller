package io.github.robertomike.super_controller.reactive.examples.mappers

import io.github.robertomike.super_controller.reactive.examples.models.User
import io.github.robertomike.super_controller.mappers.ResponseMapper
import io.github.robertomike.super_controller.reactive.examples.responses.UserResponse
import org.mapstruct.Mapper

@Mapper
interface UserResponseMapper : ResponseMapper<User, UserResponse, UserResponse>