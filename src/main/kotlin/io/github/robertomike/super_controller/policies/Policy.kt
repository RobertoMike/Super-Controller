package io.github.robertomike.super_controller.policies

import io.github.robertomike.super_controller.requests.Request

abstract class Policy<ID> {
    abstract fun viewAll(): Boolean

    abstract fun store(request: Request): Boolean

    abstract fun view(id: ID): Boolean

    abstract fun update(id: ID, request: Request): Boolean

    abstract fun destroy(id: ID): Boolean
}
