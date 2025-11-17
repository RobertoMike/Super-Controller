package io.github.robertomike.super_controller.examples.mappers

import io.github.robertomike.super_controller.examples.models.User
import io.github.robertomike.super_controller.examples.responses.UserResponseV2
import io.github.robertomike.super_controller.mappers.ResponseMapper
import org.mapstruct.Mapper

@Mapper
interface UserResponseMapperV2 : ResponseMapper<User, UserResponseV2, UserResponseV2>
