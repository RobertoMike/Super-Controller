package io.github.robertomike.super_controller.policies

import io.github.robertomike.super_controller.requests.Request

abstract class Policy<Model, out SR: Request, out UR: Request>: BasePolicy<Model, SR, UR, Boolean>()