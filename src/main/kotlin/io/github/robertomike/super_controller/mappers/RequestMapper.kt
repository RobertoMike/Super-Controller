package io.github.robertomike.super_controller.mappers

import io.github.robertomike.super_controller.requests.Request
import org.mapstruct.MappingTarget

interface RequestMapper<M, SR : Request, UR : Request> {
    fun map(request: SR): M
    fun update(request: UR, @MappingTarget model: M)
}