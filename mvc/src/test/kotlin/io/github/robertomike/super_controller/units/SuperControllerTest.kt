package io.github.robertomike.super_controller.units

import io.github.robertomike.super_controller.BasicTest
import io.github.robertomike.super_controller.controllers.SuperController
import io.github.robertomike.super_controller.enums.Methods.INDEX
import io.github.robertomike.super_controller.enums.Methods.SHOW
import io.github.robertomike.super_controller.enums.Methods.STORE
import io.github.robertomike.super_controller.examples.controllers.OrderController
import io.github.robertomike.super_controller.examples.models.Order
import io.github.robertomike.super_controller.exceptions.ServerException
import io.github.robertomike.super_controller.exceptions.SuperControllerException
import io.github.robertomike.super_controller.requests.Request
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.util.ReflectionTestUtils

class SuperControllerTest : BasicTest() {
    @Autowired
    private lateinit var controller: OrderController

    @Test
    fun emptyUrls_errors() {
        assertThrows<ServerException> {
            val temporal = object : SuperController<Order, Any, Request, Request>() {
                override fun setConfig() {
                    onlyUrls = mutableListOf()
                }
            }

            temporal.setConfig()
            temporal.urls
        }
    }

    @Test
    fun emptyAfterExceptUrls_errors() {
        assertThrows<ServerException> {
            val temporal = object : SuperController<Order, Any, Request, Request>() {
                override fun setConfig() {
                    onlyUrls = mutableListOf(INDEX)
                    exceptUrls = mutableListOf(INDEX)
                }
            }

            temporal.setConfig()
            temporal.urls
        }
    }

    @Test
    fun executePolicy_errors() {
        assertThrows<SuperControllerException> {
            invokeExecutePolicy(STORE, null, null)
        }
        assertThrows<SuperControllerException> {
            invokeExecutePolicy(SHOW, null, null)
        }
    }

    // Spring 7's ReflectionTestUtils.invokeMethod now declares its vararg as
    // JSpecify non-null Any, which Kotlin enforces at compile time - but
    // executePolicy's own model/request parameters are genuinely nullable, and
    // this test exercises that null path. An Array<Any?> holds nulls fine at
    // runtime (JVM arrays don't enforce element nullability); the unchecked
    // cast only relabels the static type so it satisfies the vararg's signature.
    private fun invokeExecutePolicy(vararg args: Any?): Boolean? {
        @Suppress("UNCHECKED_CAST")
        return ReflectionTestUtils.invokeMethod<Boolean>(
            controller, "executePolicy", *(args as Array<Any>)
        )
    }
}
