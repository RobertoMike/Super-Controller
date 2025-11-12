package io.github.robertomike.super_controller.units

import io.github.robertomike.super_controller.versioning.VersionStrategy
import io.github.robertomike.super_controller.versioning.VersioningConfig
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VersioningConfigTest {

    @Test
    fun `should create config with default values`() {
        val config = VersioningConfig()

        assertEquals("v1", config.defaultVersion)
        assertEquals(VersionStrategy.URI, config.strategy)
        assertEquals("X-API-Version", config.headerName)
        assertEquals("version", config.paramName)
        assertEquals("application/vnd.api", config.mediaTypePrefix)
        assertTrue(config.addDeprecationHeaders)
    }

    @Test
    fun `should create config with custom values`() {
        val config = VersioningConfig(
            defaultVersion = "v2",
            strategy = VersionStrategy.HEADER,
            headerName = "API-Version",
            paramName = "v",
            mediaTypePrefix = "application/vnd.myapi",
            addDeprecationHeaders = false
        )

        assertEquals("v2", config.defaultVersion)
        assertEquals(VersionStrategy.HEADER, config.strategy)
        assertEquals("API-Version", config.headerName)
        assertEquals("v", config.paramName)
        assertEquals("application/vnd.myapi", config.mediaTypePrefix)
        assertEquals(false, config.addDeprecationHeaders)
    }

    @Test
    fun `validate should pass for valid configuration`() {
        val config = VersioningConfig(
            defaultVersion = "v1",
            headerName = "X-API-Version",
            paramName = "version",
            mediaTypePrefix = "application/vnd.api"
        )

        // Should not throw
        config.validate()
    }

    @Test
    fun `validate should throw for blank default version`() {
        val config = VersioningConfig(defaultVersion = "")

        val exception = assertThrows<IllegalArgumentException> {
            config.validate()
        }

        assertEquals("Default version cannot be blank", exception.message)
    }

    @Test
    fun `validate should throw for blank header name`() {
        val config = VersioningConfig(headerName = "")

        val exception = assertThrows<IllegalArgumentException> {
            config.validate()
        }

        assertEquals("Header name cannot be blank", exception.message)
    }

    @Test
    fun `validate should throw for blank param name`() {
        val config = VersioningConfig(paramName = "")

        val exception = assertThrows<IllegalArgumentException> {
            config.validate()
        }

        assertEquals("Parameter name cannot be blank", exception.message)
    }

    @Test
    fun `validate should throw for blank media type prefix`() {
        val config = VersioningConfig(mediaTypePrefix = "")

        val exception = assertThrows<IllegalArgumentException> {
            config.validate()
        }

        assertEquals("Media type prefix cannot be blank", exception.message)
    }

    @Test
    fun `validate should throw for whitespace-only default version`() {
        val config = VersioningConfig(defaultVersion = "   ")

        val exception = assertThrows<IllegalArgumentException> {
            config.validate()
        }

        assertEquals("Default version cannot be blank", exception.message)
    }

    @Test
    fun `should support all version strategies`() {
        val strategies = listOf(
            VersionStrategy.URI,
            VersionStrategy.HEADER,
            VersionStrategy.PARAMETER,
            VersionStrategy.ACCEPT_HEADER
        )

        strategies.forEach { strategy ->
            val config = VersioningConfig(strategy = strategy)
            assertEquals(strategy, config.strategy)
        }
    }

    @Test
    fun `should be mutable data class`() {
        val config = VersioningConfig()

        config.defaultVersion = "v3"
        config.strategy = VersionStrategy.PARAMETER
        config.headerName = "Custom-Header"
        config.paramName = "apiVersion"
        config.mediaTypePrefix = "application/custom"
        config.addDeprecationHeaders = false

        assertEquals("v3", config.defaultVersion)
        assertEquals(VersionStrategy.PARAMETER, config.strategy)
        assertEquals("Custom-Header", config.headerName)
        assertEquals("apiVersion", config.paramName)
        assertEquals("application/custom", config.mediaTypePrefix)
        assertEquals(false, config.addDeprecationHeaders)
    }
}
