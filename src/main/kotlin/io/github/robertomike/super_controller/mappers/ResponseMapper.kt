package io.github.robertomike.super_controller.mappers

import io.github.robertomike.super_controller.responses.Response

interface ResponseMapper<M, DR : Response, LR : Response> {
    fun mapDetail(model: M): DR
    fun mapList(model: M): LR
}