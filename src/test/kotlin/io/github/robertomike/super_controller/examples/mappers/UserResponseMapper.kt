package io.github.robertomike.super_controller.examples.mappers

import io.github.robertomike.super_controller.examples.models.User
import io.github.robertomike.super_controller.examples.responses.UserResponse
import io.github.robertomike.super_controller.mappers.ResponseMapper
import org.mapstruct.Mapper

@Mapper
interface UserResponseMapper : ResponseMapper<User, UserResponse, UserResponse>