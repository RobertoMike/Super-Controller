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
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.util.ReflectionTestUtils

class SuperControllerTest : BasicTest() {
    @Autowired
    private lateinit var controller: OrderController

    @Test
    fun findRequestFor_ThrowError() {
        assertThrows<SuperControllerException> {
            controller.findRequestForAndMap(INDEX, "{}")
        }
    }

    @Test
    fun emptyUrls_errors() {
        assertThrows<ServerException> {
            val temporal = object : SuperController<Order, Any>() {
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
            val temporal = object : SuperController<Order, Any>() {
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
            ReflectionTestUtils.invokeMethod(controller, "executePolicy", STORE, null, null)
        }
        assertThrows<SuperControllerException> {
            ReflectionTestUtils.invokeMethod(controller, "executePolicy", SHOW, null, null)
        }
    }
}
