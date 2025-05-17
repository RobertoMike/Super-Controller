package io.github.robertomike.super_controller.services.interfaces

import io.github.robertomike.super_controller.exceptions.SuperControllerException
import io.github.robertomike.super_controller.mappers.RequestMapper
import io.github.robertomike.super_controller.requests.Request

interface MappingActions<M, out SR : Request, out UR : Request> {
    /**
     * The mapper used for to map requests and responses for business logic.
     */
    val mapper: RequestMapper<M, @UnsafeVariance SR, @UnsafeVariance UR>
        get() {
            throw SuperControllerException("Mapper not implemented")
        }

    /**
     * Maps the request to the model for store operation.
     *
     * @param request The request.
     */
    fun mappingStore(request: @UnsafeVariance SR): M {
        return mapper.map(request)
    }

    /**
     * Maps the request to the model for update operation.
     *
     * @param request The request.
     */
    fun mappingUpdate(request: @UnsafeVariance UR, target: M) {
        mapper.update(request, target)
    }
}