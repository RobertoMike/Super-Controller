package io.github.robertomike.super_controller.units

import io.github.robertomike.super_controller.config.ConfigProperties
import jakarta.validation.Validator
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertEquals

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@AutoConfigureMockMvc
class ConfigPropertiesTest {
    @Autowired
    private lateinit var validator: Validator

    @Test
    fun configProperties() {
        val configProperties = ConfigProperties()

        var violations = validator.validate(configProperties)
        assertEquals(15, violations.size)

        configProperties.basePackage = "io"
        configProperties.prefixUrl = "/api"
        configProperties.path.policies = "policies"
        configProperties.path.requests = "DTO.requests"
        configProperties.path.services = "dto_requests"
        configProperties.path.responses = "Responses"
        configProperties.classSuffix.policy = "Policy"
        configProperties.classSuffix.request = "RequestRandomSuffix"
        configProperties.classSuffix.service = "Service"
        configProperties.classSuffix.response = "Response"

        violations = validator.validate(configProperties)
        assertTrue(violations.isEmpty())
    }
}