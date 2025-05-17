package io.github.robertomike.super_controller.reactive.examples.mappers

import io.github.robertomike.super_controller.mappers.RequestMapper
import io.github.robertomike.super_controller.reactive.examples.models.Order
import io.github.robertomike.super_controller.reactive.examples.requests.order.StoreOrderRequest
import io.github.robertomike.super_controller.reactive.examples.requests.order.UpdateOrderRequest
import org.mapstruct.Mapper

@Mapper
interface OrderMapper : RequestMapper<Order, StoreOrderRequest, UpdateOrderRequest>