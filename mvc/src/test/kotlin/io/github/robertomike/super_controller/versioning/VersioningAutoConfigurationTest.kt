package io.github.robertomike.super_controller.versioning

import io.github.robertomike.super_controller.BasicTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests for versioning auto-configuration.
 */
@SpringBootTest
@TestPropertySource(properties = [
    "super-controller.versioning.enabled=true",
    "super-controller.versioning.default-version=v2",
    "super-controller.versioning.strategy=HEADER",
    "super-controller.versioning.add-deprecation-headers=true"
])
class VersioningAutoConfigurationTest : BasicTest() {

    @Autowired(required = false)
    private var versioningConfig: VersioningConfig? = null

    @Autowired(required = false)
    private var apiVersionInterceptor: ApiVersionInterceptor? = null

    @Test
    fun `versioning configuration is auto-configured`() {
        assertNotNull(versioningConfig, "VersioningConfig should be auto-configured")
        assertEquals("v2", versioningConfig?.defaultVersion)
        assertEquals(VersionStrategy.HEADER, versioningConfig?.strategy)
        assertTrue(versioningConfig?.addDeprecationHeaders == true)
    }

    @Test
    fun `api version interceptor is auto-configured`() {
        assertNotNull(apiVersionInterceptor, "ApiVersionInterceptor should be auto-configured")
    }
}

/**
 * Tests that versioning is not configured when disabled.
 */
@SpringBootTest
@TestPropertySource(properties = [
    "super-controller.versioning.enabled=false"
])
class VersioningDisabledTest : BasicTest() {

    @Autowired(required = false)
    private var versioningConfig: VersioningConfig? = null

    @Autowired(required = false)
    private var apiVersionInterceptor: ApiVersionInterceptor? = null

    @Test
    fun `versioning is not configured when disabled`() {
        assertEquals(null, versioningConfig, "VersioningConfig should not be configured when disabled")
        assertEquals(null, apiVersionInterceptor, "ApiVersionInterceptor should not be configured when disabled")
    }
}
