package io.github.robertomike.super_controller.units

import io.github.robertomike.super_controller.config.ClassSuffix
import io.github.robertomike.super_controller.config.ConfigProperties
import io.github.robertomike.super_controller.config.Path
import io.github.robertomike.super_controller.exceptions.SuperControllerException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class ConfigPropertiesTest {
    @Test
    fun configProperties() {
        val configProperties = ConfigProperties()

        assertThrows<SuperControllerException>(configProperties::init)
        configProperties.prefixUrl = "/api/"

        assertThrows<SuperControllerException>(configProperties::init)
        configProperties.basePackage = "io.github.robertomike.super_controller"
    }

    @Test
    fun verifyPath() {
        val path = Path()

        assertThrows<SuperControllerException>(path::verify)
        path.requests = "requests"

        assertThrows<SuperControllerException>(path::verify)
        path.responses = "responses"

        assertThrows<SuperControllerException>(path::verify)
        path.policies = "policies"

        assertThrows<SuperControllerException>(path::verify)
        path.services = "services"
    }

    @Test
    fun verifyClassSuffix() {
        val classSuffix = ClassSuffix()

        assertThrows<SuperControllerException>(classSuffix::verify)
        classSuffix.request = "Request"

        assertThrows<SuperControllerException>(classSuffix::verify)
        classSuffix.response = "Response"

        assertThrows<SuperControllerException>(classSuffix::verify)
        classSuffix.policy = "Policy"

        assertThrows<SuperControllerException>(classSuffix::verify)
        classSuffix.service = "Service"
    }
}