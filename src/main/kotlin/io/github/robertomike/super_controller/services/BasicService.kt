package io.github.robertomike.super_controller.services

import io.github.robertomike.hefesto.models.BaseModel
import io.github.robertomike.super_controller.requests.Request
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest

interface BasicService<M : BaseModel?, ID> {
    fun index(page: PageRequest): Page<M>

    fun store(request: Request): M

    fun show(id: ID): M

    fun update(id: ID, request: Request): M

    fun delete(id: ID)
}
