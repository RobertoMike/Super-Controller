package io.github.robertomike.super_controller.policies

import io.github.robertomike.super_controller.requests.Request

abstract class Policy<ID, out SR: Request, out UR: Request>: BasePolicy<ID, SR, UR, Boolean>()