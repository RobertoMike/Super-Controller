package io.github.robertomike.super_controller.units

import io.github.robertomike.super_controller.versioning.ApiVersionRequestCondition
import io.github.robertomike.super_controller.versioning.VersionStrategy
import io.github.robertomike.super_controller.versioning.VersioningConfig
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ApiVersionRequestConditionTest {

    @Test
    fun `should match request with URI strategy`() {
        val config = VersioningConfig(strategy = VersionStrategy.URI)
        val condition = ApiVersionRequestCondition("v1", config)
        val request = createRequest("/v1/users", null, null, null)

        val result = condition.getMatchingCondition(request)

        assertNotNull(result)
    }

    @Test
    fun `should not match request with different URI version`() {
        val config = VersioningConfig(strategy = VersionStrategy.URI)
        val condition = ApiVersionRequestCondition("v1", config)
        val request = createRequest("/v2/users", null, null, null)

        val result = condition.getMatchingCondition(request)

        assertNull(result)
    }

    @Test
    fun `should match request with HEADER strategy`() {
        val config = VersioningConfig(
            strategy = VersionStrategy.HEADER,
            headerName = "X-API-Version"
        )
        val condition = ApiVersionRequestCondition("v1", config)
        val request = createRequest("/users", "v1", null, null)

        val result = condition.getMatchingCondition(request)

        assertNotNull(result)
    }

    @Test
    fun `should not match request with different header version`() {
        val config = VersioningConfig(
            strategy = VersionStrategy.HEADER,
            headerName = "X-API-Version"
        )
        val condition = ApiVersionRequestCondition("v1", config)
        val request = createRequest("/users", "v2", null, null)

        val result = condition.getMatchingCondition(request)

        assertNull(result)
    }

    @Test
    fun `should match request with PARAMETER strategy`() {
        val config = VersioningConfig(
            strategy = VersionStrategy.PARAMETER,
            paramName = "version"
        )
        val condition = ApiVersionRequestCondition("v1", config)
        val request = createRequest("/users", null, "v1", null)

        val result = condition.getMatchingCondition(request)

        assertNotNull(result)
    }

    @Test
    fun `should not match request with different parameter version`() {
        val config = VersioningConfig(
            strategy = VersionStrategy.PARAMETER,
            paramName = "version"
        )
        val condition = ApiVersionRequestCondition("v1", config)
        val request = createRequest("/users", null, "v2", null)

        val result = condition.getMatchingCondition(request)

        assertNull(result)
    }

    @Test
    fun `should match request with ACCEPT_HEADER strategy`() {
        val config = VersioningConfig(
            strategy = VersionStrategy.ACCEPT_HEADER,
            mediaTypePrefix = "application/vnd.api"
        )
        val condition = ApiVersionRequestCondition("v1", config)
        val request = createRequest("/users", null, null, "application/vnd.api.v1+json")

        val result = condition.getMatchingCondition(request)

        assertNotNull(result)
    }

    @Test
    fun `should not match request with different accept header version`() {
        val config = VersioningConfig(
            strategy = VersionStrategy.ACCEPT_HEADER,
            mediaTypePrefix = "application/vnd.api"
        )
        val condition = ApiVersionRequestCondition("v1", config)
        val request = createRequest("/users", null, null, "application/vnd.api.v2+json")

        val result = condition.getMatchingCondition(request)

        assertNull(result)
    }

    @Test
    fun `should use default version when no version in request`() {
        val config = VersioningConfig(
            strategy = VersionStrategy.HEADER,
            headerName = "X-API-Version",
            defaultVersion = "v1"
        )
        val condition = ApiVersionRequestCondition("v1", config)
        val request = createRequest("/users", null, null, null)

        val result = condition.getMatchingCondition(request)

        assertNotNull(result, "Should match using default version")
    }

    @Test
    fun `should combine conditions preferring one with version`() {
        val config = VersioningConfig()
        val condition1 = ApiVersionRequestCondition("v1", config)
        val condition2 = ApiVersionRequestCondition("v2", config)

        val result = condition1.combine(condition2)

        assertEquals("v2", result.toString().substringAfter("version='").substringBefore("'"))
    }

    @Test
    fun `should compare conditions preferring ones with versions`() {
        val config = VersioningConfig()
        val conditionWithVersion = ApiVersionRequestCondition("v1", config)
        val conditionWithoutVersion = ApiVersionRequestCondition("", config)
        val request = mock(HttpServletRequest::class.java)

        val result = conditionWithVersion.compareTo(conditionWithoutVersion, request)

        assertEquals(-1, result, "Condition with version should be more specific")
    }

    @Test
    fun `should extract version from complex URI paths`() {
        val config = VersioningConfig(strategy = VersionStrategy.URI)
        val condition = ApiVersionRequestCondition("v2", config)
        val request = createRequest("/api/v2/users/123/orders", null, null, null)

        val result = condition.getMatchingCondition(request)

        assertNotNull(result, "Should extract v2 from /api/v2/users/123/orders")
    }

    @Test
    fun `should handle Accept header with multiple media types`() {
        val config = VersioningConfig(
            strategy = VersionStrategy.ACCEPT_HEADER,
            mediaTypePrefix = "application/vnd.api"
        )
        val condition = ApiVersionRequestCondition("v1", config)
        val request = createRequest(
            "/users", 
            null, 
            null, 
            "application/json, application/vnd.api.v1+json, text/html"
        )

        val result = condition.getMatchingCondition(request)

        assertNotNull(result, "Should find version in Accept header with multiple types")
    }

    private fun createRequest(
        uri: String,
        apiVersionHeader: String?,
        versionParam: String?,
        acceptHeader: String?
    ): HttpServletRequest {
        val request = mock(HttpServletRequest::class.java)
        `when`(request.requestURI).thenReturn(uri)
        `when`(request.getHeader("X-API-Version")).thenReturn(apiVersionHeader)
        `when`(request.getParameter("version")).thenReturn(versionParam)
        `when`(request.getHeader("Accept")).thenReturn(acceptHeader)
        return request
    }
}
