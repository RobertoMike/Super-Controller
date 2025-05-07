package io.github.robertomike.super_controller.examples.mappers

import io.github.robertomike.super_controller.examples.models.Order
import io.github.robertomike.super_controller.examples.requests.order.StoreOrderRequest
import io.github.robertomike.super_controller.examples.requests.order.UpdateOrderRequest
import io.github.robertomike.super_controller.mappers.RequestMapper
import org.mapstruct.Mapper

@Mapper
interface OrderMapper : RequestMapper<Order, StoreOrderRequest, UpdateOrderRequest>